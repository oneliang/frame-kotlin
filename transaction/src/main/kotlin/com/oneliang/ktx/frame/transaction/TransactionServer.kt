package com.oneliang.ktx.frame.transaction

import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

class TransactionServer(host: String, port: Int, maxThreadCount: Int = Runtime.getRuntime().availableProcessors()) : SelectorProcessor {

    companion object {
        private val logger = LoggerManager.getLogger(TransactionServer::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val idSocketChannelMap = ConcurrentHashMap<String, Int>()
    private val server = Server(host, port, maxThreadCount).also { it.selectorProcessor = this }
    private val atomicMap = AtomicMap<String, Transaction>()

    class Transaction(var masterId: String, var idHashSet: HashSet<String> = hashSetOf())

    fun start() {
        this.server.start()
    }

    fun stop() {
        this.server.stop()
    }

    override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
        val tlvPacket = tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
        val requestString = String(tlvPacket.body)
        logger.debug("server read:%s", requestString)
        val transactionRequest = requestString.jsonToObject(TransactionRequest::class)
        val action = transactionRequest.action
        val masterId = transactionRequest.masterId
        val id = transactionRequest.id
        this.idSocketChannelMap[id] = socketChannelHashCode
        return if (action == ConstantsTransaction.Action.BEGIN_TRANSACTION) {
            var success = false
            this.atomicMap.operate(masterId, create = {
                success = true
                Transaction(masterId)
            }, update = { oldTransaction ->
                success = false
                logger.error("can not beginTransaction twice and more times, master id:%s", masterId)
                Transaction(oldTransaction.masterId)
            })
            logger.debug("begin transaction, id:%s, request:%s", id, atomicMap.toJson())
            val transactionResponseJson = TransactionResponse.buildBeginTransactionResponse(masterId, id, success).toJson()
            TlvPacket(ConstantsTransaction.TlvPackageType.BEGIN, transactionResponseJson.toByteArray()).toByteArray()
        } else if (action == ConstantsTransaction.Action.END_TRANSACTION) {
            val transactionResponseJson = TransactionResponse.buildEndTransactionResponse(masterId, id, true).toJson()
            TlvPacket(ConstantsTransaction.TlvPackageType.END, transactionResponseJson.toByteArray()).toByteArray()
        } else if (action == ConstantsTransaction.Action.EXECUTE_IN_TRANSACTION) {
            var success = false
            this.atomicMap.operate(masterId, create = {
                success = false
                logger.error("can not executeInTransaction before beginTransaction, master id:%s", masterId)
                Transaction(masterId)
            }, update = { oldTransaction ->
                success = true
                Transaction(oldTransaction.masterId).also {
                    if (oldTransaction.masterId != id) {//may be begin transaction twice
                        //only use the same hashset
                        oldTransaction.idHashSet += id
                    }
                    it.idHashSet = oldTransaction.idHashSet
                }
            })
            if (!success) {//remove it when execute in error branch
                this.atomicMap.remove(masterId)
            }
            val transactionResponseJson = TransactionResponse.buildExecuteInTransactionResponse(masterId, id, success).toJson()
            TlvPacket(ConstantsTransaction.TlvPackageType.EXECUTE_IN, transactionResponseJson.toByteArray()).toByteArray()
        } else {
            val transaction = this.atomicMap[masterId]
            var success = false
            if (transaction == null) {
                logger.error("maybe error logic branch, can not find the transaction, rollbackTransaction must execute after beginTransaction, master id:%s", masterId)
            } else {
                success = true
                transaction.idHashSet.forEach {
                    //notify
                    val transactionResponseJson = TransactionResponse.buildRollbackTransactionResponse(masterId, it, success).toJson()
                    val notifyByteArray = TlvPacket(ConstantsTransaction.TlvPackageType.ROLLBACK, transactionResponseJson.toByteArray()).toByteArray()
                    //just write, because has special data
                    val removedId = this.idSocketChannelMap[id]
                    this.server.notify(removedId ?: 0, notifyByteArray)
                }
            }
            val transactionResponseJson = TransactionResponse.buildRollbackTransactionResponse(masterId, id, success).toJson()
            TlvPacket(ConstantsTransaction.TlvPackageType.ROLLBACK, transactionResponseJson.toByteArray()).toByteArray()
        }
    }
}