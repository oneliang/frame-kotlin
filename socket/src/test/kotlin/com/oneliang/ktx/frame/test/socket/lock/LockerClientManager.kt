package com.oneliang.ktx.frame.test.socket.lock

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.TlvPacket
import com.oneliang.ktx.frame.socket.TlvPacketProcessor
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.PID
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.concurrent.atomic.AwaitAndSignal
import com.oneliang.ktx.util.json.JsonObject
import com.oneliang.ktx.util.json.jsonToJsonObject
import com.oneliang.ktx.util.logging.LoggerManager

private fun tryLockRequestJson(lockKey: String, id: String): String {
    val requestJsonObject = JsonObject()
    requestJsonObject.put("action", "tryLock")
    requestJsonObject.put("id", id)
    requestJsonObject.put("lockKey", lockKey)
    return requestJsonObject.toString()
}

private fun releaseLockRequestJson(lockKey: String, id: String): String {
    val requestJsonObject = JsonObject()
    requestJsonObject.put("action", "releaseLock")
    requestJsonObject.put("id", id)
    requestJsonObject.put("lockKey", lockKey)
    return requestJsonObject.toString()
}

class LocalLocker(val lock: String, val lockId: String, var lockingIdHashSet: HashSet<String> = hashSetOf())

class LockerClientManager(private val host: String, private val port: Int) : Function1<ByteArray, Unit> {
    companion object {
        private val logger = LoggerManager.getLogger(LockerClient::class)
    }

    private val clientManager = ClientManager(this.host, this.port, this)
    private val tlvPacketProcessor = TlvPacketProcessor()
    private val atomicMap = AtomicMap<String, LocalLocker>()
    private val awaitAndSignal = AwaitAndSignal<String>()

    //clientManager readProcessor is lambda, so you can implement Function interface to use it
    override fun invoke(p1: ByteArray) {
        val responseTlvPacket = this.tlvPacketProcessor.receiveTlvPacket(p1)
        val responseJson = String(responseTlvPacket.body)
        logger.debug("receive, type:%s, body json:%s", responseTlvPacket.type.toInt(), responseJson)
        val responseJsonObject = responseJson.jsonToJsonObject()
        val action = responseJsonObject.optString("action")
        val id = responseJsonObject.optString("id")
        val success = responseJsonObject.optBoolean("success")
        when {
            action == "tryLock" && success -> {
                this.awaitAndSignal.signal(id)
            }
            action == "releaseLock" && success -> {
            }
        }
    }

    init {
        this.clientManager.start()
    }

    private fun generateGlobalThreadId(): String {
        val tid = Thread.currentThread().id.toString()
        //tid@pid@hostAddress, threadId+jvmId+IP
        return tid + Constants.Symbol.AT + PID + Constants.Symbol.AT + HOST_ADDRESS
    }

    fun tryLock(lockKey: String) {
        val id = generateGlobalThreadId()
        val tryLockRequestJson = tryLockRequestJson(lockKey, id)
        logger.debug("tryLock request:%s", tryLockRequestJson)
        val tlvPacket = TlvPacket(1.toByteArray(), tryLockRequestJson.toByteArray())
        this.atomicMap.operate(lockKey, create = {
            //need to send
            this.clientManager.send(tlvPacket.toByteArray())
            LocalLocker(lockKey, id)
        }, update = { oldClientLocker ->
            LocalLocker(oldClientLocker.lock, oldClientLocker.lockId).also {
                //only use the same hashset
                oldClientLocker.lockingIdHashSet += id
                it.lockingIdHashSet = oldClientLocker.lockingIdHashSet
            }
        })
        this.awaitAndSignal.await(id)
        logger.debug("tryLock[%s] success", lockKey)
    }

    fun releaseLock(lockKey: String) {
        val id = generateGlobalThreadId()
        val releaseLockRequestJson = releaseLockRequestJson(lockKey, id)
        logger.debug("releaseLock request:%s", releaseLockRequestJson)
        val tlvPacket = TlvPacket(1.toByteArray(), releaseLockRequestJson.toByteArray())
        this.clientManager.send(tlvPacket.toByteArray())
    }
}