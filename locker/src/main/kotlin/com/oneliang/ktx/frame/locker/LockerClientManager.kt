package com.oneliang.ktx.frame.locker

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
import com.oneliang.ktx.util.json.jsonToJsonObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

class LockerClientManager(host: String, port: Int) : Function1<ByteArray, Unit> {
    companion object {
        private val logger = LoggerManager.getLogger(LockerClientManager::class)
    }

    private val clientManager = ClientManager(host, port, this)
    private val tlvPacketProcessor = TlvPacketProcessor()
    private val atomicMap = AtomicMap<String, LocalLocker>()
    private val awaitAndSignal = AwaitAndSignal<String>()

    init {
        this.clientManager.start()
    }

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

    private fun generateGlobalThreadId(): String {
        val tid = Thread.currentThread().id.toString()
        //tid@pid@hostAddress, threadId+jvmId+IP
        return tid + Constants.Symbol.AT + PID + Constants.Symbol.AT + HOST_ADDRESS
    }

    fun tryLock(lockKey: String) {
        val id = generateGlobalThreadId()
        val tryLockRequestJson = LockRequest.buildTryLockRequest(lockKey, id).toJson()
        logger.debug("tryLock request:%s", tryLockRequestJson)
        val tlvPacket = TlvPacket(ConstantsLock.TlvPackageType.REQUEST_RESPONSE, tryLockRequestJson.toByteArray())
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
        val releaseLockRequestJson = LockRequest.buildReleaseLockRequest(lockKey, id).toJson()
        logger.debug("releaseLock request:%s", releaseLockRequestJson)
        val tlvPacket = TlvPacket(ConstantsLock.TlvPackageType.REQUEST_RESPONSE, releaseLockRequestJson.toByteArray())
        this.clientManager.send(tlvPacket.toByteArray())
    }

    private class LocalLocker(val lock: String, val lockId: String, var lockingIdHashSet: HashSet<String> = hashSetOf())
}
