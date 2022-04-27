package com.oneliang.ktx.frame.locker

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.TlvPacket
import com.oneliang.ktx.frame.socket.TlvPacketProcessor
import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

class LockerServer(host: String, port: Int, maxThreadCount: Int = Runtime.getRuntime().availableProcessors()) : SelectorProcessor {

    companion object {
        private val logger = LoggerManager.getLogger(LockerServer::class)
    }

    private val idSocketChannelMap = ConcurrentHashMap<String, Int>()
    private val tlvPacketProcessor = TlvPacketProcessor()
    private val server = Server(host, port, maxThreadCount).also { it.selectorProcessor = this }
    private val atomicMap = AtomicMap<String, Locker>()

    class Locker(val lock: String, var lockId: String, var lockingIdHashSet: HashSet<String> = hashSetOf())

    fun start() {
        this.server.start()
    }

    fun stop() {
        this.server.stop()
    }

    override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
        val tlvPacket = this.tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
        val requestString = String(tlvPacket.body)
        logger.debug("server read:%s", requestString)
        val lockRequest = requestString.jsonToObject(LockRequest::class)
        val action = lockRequest.action
        val id = lockRequest.id
        val lockKey = lockRequest.lockKey
        this.idSocketChannelMap[id] = socketChannelHashCode
        return if (action == ConstantsLock.Action.TRY_LOCK) {
            var success = false
            this.atomicMap.operate(lockKey, create = {
                success = true
                Locker(lockKey, id)
            }, update = { oldLocker ->
                Locker(oldLocker.lock, oldLocker.lockId).also {
                    if (oldLocker.lockId != id) {//may be try lock twice, same socketChannel and clientThreadId
                        //only use the same hashset
                        oldLocker.lockingIdHashSet += id
                    }
                    it.lockingIdHashSet = oldLocker.lockingIdHashSet
                }
            })
            logger.debug("tryLock, id:%s, request:%s", id, atomicMap.toJson())
            val tryLockResponseJson = LockResponse.buildTryLockResponse(id, success).toJson()
            TlvPacket(ConstantsLock.TlvPackageType.REQUEST_RESPONSE, tryLockResponseJson.toByteArray()).toByteArray()
        } else {
            var success = false
            var needToRemove = false
            var notifyId = Constants.String.BLANK
            this.atomicMap.operate(lockKey, create = {
                //maybe invoke release lock before try lock
                success = true
                needToRemove = true
                Locker(lockKey, id)
            }, update = { oldLocker ->
                if (oldLocker.lockingIdHashSet.isNotEmpty()) {
                    val iterator = oldLocker.lockingIdHashSet.iterator()
                    if (iterator.hasNext()) {
                        notifyId = iterator.next()
                        iterator.remove()
                    }
                    success = true
                    Locker(oldLocker.lock, notifyId).also {
                        it.lockingIdHashSet = oldLocker.lockingIdHashSet
                    }
                } else {//locking id hashset is empty, just normal release, no other locking id
                    if (id == oldLocker.lockId) {
                        success = true
                        needToRemove = true
                    } else {
                        //client use case error
                    }
                    Locker(oldLocker.lock, oldLocker.lockId)
                }
            })
            logger.debug("releaseLock, id:%s, notify id:%s, request:%s", id, notifyId, atomicMap.toJson())
            if (success) {
                if (needToRemove) {
                    this.atomicMap.remove(lockKey)
                    val releaseLockResponseJson = LockResponse.buildReleaseLockResponse(id, success).toJson()
                    return TlvPacket(ConstantsLock.TlvPackageType.REQUEST_RESPONSE, releaseLockResponseJson.toByteArray()).toByteArray()
                } else {
                    //notify
                    val tryLockResponseJson = LockResponse.buildTryLockResponse(notifyId, success).toJson()
                    val notifyByteArray = TlvPacket(ConstantsLock.TlvPackageType.NOTIFY, tryLockResponseJson.toByteArray()).toByteArray()
                    //just write, because has special data
                    val removedNotifyId = this.idSocketChannelMap.remove(notifyId)
                    this.server.notify(removedNotifyId ?: 0, notifyByteArray)
                }
            }
            val releaseLockResponseJson = LockResponse.buildReleaseLockResponse(id, success).toJson()
            TlvPacket(ConstantsLock.TlvPackageType.REQUEST_RESPONSE, releaseLockResponseJson.toByteArray()).toByteArray()
        }
    }
}