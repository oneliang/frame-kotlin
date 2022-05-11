package com.oneliang.ktx.frame.transaction

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.PID
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.concurrent.atomic.AwaitAndSignal
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.util.concurrent.ConcurrentHashMap

class TransactionClientManager(host: String, port: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(TransactionClientManager::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val awaitAndSignal = AwaitAndSignal<String>()
    private val rollbackMap = ConcurrentHashMap<String, () -> Unit>()

    //second way:clientManager readProcessor is lambda, so you can implement Function interface to use it
    private val readProcessor = { byteArray: ByteArray ->
        val responseTlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
        val responseJson = String(responseTlvPacket.body)
        logger.debug("receive, type:%s, body json:%s", responseTlvPacket.type.toInt(), responseJson)
        val transactionResponse = responseJson.jsonToObject(TransactionResponse::class)
        val action = transactionResponse.action
        val id = transactionResponse.id
        val success = transactionResponse.success
        when {
            (action == ConstantsTransaction.Action.BEGIN_TRANSACTION
                    || action == ConstantsTransaction.Action.END_TRANSACTION
                    || action == ConstantsTransaction.Action.EXECUTE_IN_TRANSACTION) && success -> {
                this.awaitAndSignal.signal(id)
            }
            action == ConstantsTransaction.Action.ROLLBACK_TRANSACTION -> {
                val rollbackBlock = this.rollbackMap[id]
                if (rollbackBlock != null) {
                    rollbackBlock()
                }
            }
        }
    }

    private val clientManager = ClientManager(host, port, readProcessor = this.readProcessor)

    init {
        this.clientManager.start()
    }

    private fun generateGlobalThreadId(): String {
        val tid = Thread.currentThread().id.toString()
        //tid@pid@hostAddress, threadId+jvmId+IP
        return tid + Constants.Symbol.AT + PID + Constants.Symbol.AT + HOST_ADDRESS
    }

    fun executeInTransaction(masterId: String, block: () -> Unit, rollbackBlock: () -> Unit) {
        val id = generateGlobalThreadId()
        if (masterId.isNotBlank()) {
            val executeInTransactionRequestJson = TransactionRequest.buildExecuteInTransactionRequest(masterId, masterId).toJson()
            logger.debug("end transaction request:%s", executeInTransactionRequestJson)
            val endTransactionTlvPacket = TlvPacket(ConstantsTransaction.TlvPackageType.EXECUTE_IN, executeInTransactionRequestJson.toByteArray())
            this.rollbackMap[id] = rollbackBlock
            this.clientManager.send(endTransactionTlvPacket.toByteArray())
            //send message
        } else {
            error("master id can not be blank, this method must invoke after beginTransaction and get the masterId")
        }
        this.awaitAndSignal.await(masterId)
        block()
    }

    fun executeTransaction(block: (masterId: String) -> Boolean) {
        val id = generateGlobalThreadId()
        val beginTransactionRequestJson = TransactionRequest.buildBeginTransactionRequest(id, id).toJson()
        logger.debug("begin transaction request:%s", beginTransactionRequestJson)
        val beginTransactionTlvPacket = TlvPacket(ConstantsTransaction.TlvPackageType.BEGIN, beginTransactionRequestJson.toByteArray())
        this.clientManager.send(beginTransactionTlvPacket.toByteArray())
        this.awaitAndSignal.await(id)
        val sign = block(id)
        if (sign) {
            val endTransactionRequestJson = TransactionRequest.buildEndTransactionRequest(id, id).toJson()
            logger.debug("end transaction request:%s", endTransactionRequestJson)
            val endTransactionTlvPacket = TlvPacket(ConstantsTransaction.TlvPackageType.END, endTransactionRequestJson.toByteArray())
            this.clientManager.send(endTransactionTlvPacket.toByteArray())
        } else {
            val rollbackTransactionRequestJson = TransactionRequest.buildRollbackTransactionRequest(id, id).toJson()
            logger.debug("rollback transaction request:%s", rollbackTransactionRequestJson)
            val rollbackTransactionTlvPacket = TlvPacket(ConstantsTransaction.TlvPackageType.ROLLBACK, rollbackTransactionRequestJson.toByteArray())
            this.clientManager.send(rollbackTransactionTlvPacket.toByteArray())
        }
        this.awaitAndSignal.await(id)
        logger.debug("end transaction or rollback transaction")
    }
}
