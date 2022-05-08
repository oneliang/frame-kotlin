package com.oneliang.ktx.frame.test.socket.lock

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.concurrent.atomic.AtomicMap
import com.oneliang.ktx.util.json.JsonObject
import com.oneliang.ktx.util.json.jsonToJsonObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

class Locker(val lock: String, var lockId: String, var lockingIdHashSet: HashSet<String> = hashSetOf())

private fun tryLockResponseJson(action: String, id: String, success: Boolean): String {
    val responseJsonObject = JsonObject()
    responseJsonObject.put("action", action)
    responseJsonObject.put("id", id)
    responseJsonObject.put("success", success)
    return responseJsonObject.toString()
}

private fun releaseLockResponseJson(action: String, id: String, success: Boolean): String {
    val responseJsonObject = JsonObject()
    responseJsonObject.put("action", action)
    responseJsonObject.put("id", id)
    responseJsonObject.put("success", success)
    return responseJsonObject.toString()
}

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val atomicMap = AtomicMap<String, Locker>()
    val idSocketChannelMap = ConcurrentHashMap<String, Int>()
    val tlvPacketProcessor = TlvPacketProcessor()
    val server = Server("localhost", 9999, 1)
    val socketChannelHashCodeHashSet = hashSetOf<Int>()
    server.selectorProcessor = object : SelectorProcessor {
        override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
            socketChannelHashCodeHashSet += socketChannelHashCode
            val tlvPacket = tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
            val requestString = String(tlvPacket.body)
            val requestJsonObject = requestString.jsonToJsonObject()
            println(Thread.currentThread().toString() + ", server read :$requestString")
            val action = requestJsonObject.optString("action")
            val id = requestJsonObject.optString("id")
            val lockKey = requestJsonObject.optString("lockKey")
            idSocketChannelMap[id] = socketChannelHashCode
            return if (action == "tryLock") {
                var success = false
                atomicMap.operate(lockKey, create = {
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
                println("tryLock, id:%s, request:%s".format(id, atomicMap.toJson()))
                val tryLockResponseJson = tryLockResponseJson(action, id, success)
                TlvPacket(1.toByteArray(), tryLockResponseJson.toByteArray()).toByteArray()
            } else {
                var success = false
                var needToRemove = false
                var notifyId = Constants.String.BLANK
                atomicMap.operate(lockKey, create = {
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
                println("releaseLock, id:%s, notify id:%s, request:%s".format(id, notifyId, atomicMap.toJson()))
                if (success) {
                    if (needToRemove) {
                        atomicMap.remove(lockKey)
                        val releaseLockResponseJson = releaseLockResponseJson(action, id, success)
                        return TlvPacket(1.toByteArray(), releaseLockResponseJson.toByteArray()).toByteArray()
                    } else {
                        //notify
                        val tryLockResponseJson = tryLockResponseJson("tryLock", notifyId, success)
                        val notifyByteArray = TlvPacket(2.toByteArray(), tryLockResponseJson.toByteArray()).toByteArray()
                        server.notify(idSocketChannelMap[notifyId] ?: 0, notifyByteArray)
                    }
                }
                val releaseLockResponseJson = releaseLockResponseJson(action, id, success)
                TlvPacket(1.toByteArray(), releaseLockResponseJson.toByteArray()).toByteArray()
            }
        }

        override fun notify(socketChannelHashCode: Int): ByteArray {
            val tryLockResponseJson = tryLockResponseJson("tryLock", "", true)
            return TlvPacket(2.toByteArray(), tryLockResponseJson.toByteArray()).toByteArray()

        }
    }
    server.start()
    println("----------------------------------start-------------------------------")
//    Thread.sleep(30000)
//    server.notify(socketChannelHashCodeHashSet)
//    println("----------------------------------after notify-------------------------------")
}