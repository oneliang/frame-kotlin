package com.oneliang.ktx.frame.locker

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.Generator
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.PID
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.concurrent.atomic.AwaitAndSignal
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor

class LockerClientManager(serverHost: String, serverPort: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(LockerClientManager::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val atomicMap = AtomicMap<String, LocalLocker>()
    private val awaitAndSignal = AwaitAndSignal<String>()

    //second way:clientManager readProcessor is lambda, so you can implement Function interface to use it
    private val readProcessor = { byteArray: ByteArray ->
        val responseTlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
        val responseJson = String(responseTlvPacket.body)
        logger.debug("receive, type:%s, body json:%s", responseTlvPacket.type.toInt(), responseJson)
        val lockResponse = responseJson.jsonToObject(LockResponse::class)
        val action = lockResponse.action
        val id = lockResponse.id
        val success = lockResponse.success
        when {
            action == ConstantsLock.Action.TRY_LOCK && success -> {
                this.awaitAndSignal.signal(id)
            }
            action == ConstantsLock.Action.RELEASE_LOCK && success -> {
            }
        }
    }
    private val clientManager = ClientManager(serverHost, serverPort, readProcessor = this.readProcessor)

    init {
        this.clientManager.start()
    }

    fun tryLock(lockKey: String) {
        val id = Generator.generateGlobalThreadId()
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
                oldClientLocker.lockingIdHashSet.add(id)
                it.lockingIdHashSet = oldClientLocker.lockingIdHashSet
            }
        })
        this.awaitAndSignal.await(id)
        logger.debug("tryLock[%s] success", lockKey)
    }

    fun releaseLock(lockKey: String) {
        val id = Generator.generateGlobalThreadId()
        val releaseLockRequestJson = LockRequest.buildReleaseLockRequest(lockKey, id).toJson()
        logger.debug("releaseLock request:%s", releaseLockRequestJson)
        val tlvPacket = TlvPacket(ConstantsLock.TlvPackageType.REQUEST_RESPONSE, releaseLockRequestJson.toByteArray())
        this.clientManager.send(tlvPacket.toByteArray())
    }

    private class LocalLocker(val lock: String, val lockId: String, var lockingIdHashSet: HashSet<String> = hashSetOf())
}
