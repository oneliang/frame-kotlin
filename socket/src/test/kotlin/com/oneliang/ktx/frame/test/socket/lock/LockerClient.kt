package com.oneliang.ktx.frame.test.socket.lock

import com.oneliang.ktx.frame.socket.SocketClientPool
import com.oneliang.ktx.frame.socket.SocketClientSource
import com.oneliang.ktx.frame.socket.TlvPacket
import com.oneliang.ktx.frame.socket.TlvPacketProcessor
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.json.JsonObject
import com.oneliang.ktx.util.json.jsonToJsonObject
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*

private fun tryLockRequestJson(lockKey: String): String {
    val requestJsonObject = JsonObject()
    requestJsonObject.put("action", "tryLock")
    requestJsonObject.put("lockKey", lockKey)
    return requestJsonObject.toString()
}

private fun releaseLockRequestJson(lockKey: String): String {
    val requestJsonObject = JsonObject()
    requestJsonObject.put("action", "releaseLock")
    requestJsonObject.put("lockKey", lockKey)
    return requestJsonObject.toString()
}

class LockerClient(private val host: String, private val port: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(LockerClient::class)
    }

    private val socketClientPool = SocketClientPool()
    private val tlvPacketProcessor = TlvPacketProcessor()

    init {
        this.socketClientPool.setResourceSource(SocketClientSource().also {
            it.host = host
            it.port = port
        })
        this.socketClientPool.minResourceSize = 0
        this.socketClientPool.threadSleepTime = 20000
        this.socketClientPool.resourceAliveTime = 60000
        this.socketClientPool.initialize()
    }

    fun tryLock(lockKey: String) {
        val lockSuccess = this.socketClientPool.useSocketClient {
            val tryLockRequestJson = tryLockRequestJson(lockKey)
            val tlvPacket = TlvPacket(1.toByteArray(), tryLockRequestJson.toByteArray())
            val begin = System.currentTimeMillis()
            val responseTlvPacket = it.send { outputStream, inputStream ->
                this.tlvPacketProcessor.sendTlvPacket(outputStream, tlvPacket)
                this.tlvPacketProcessor.receiveTlvPacket(inputStream)
            }
            if (responseTlvPacket != null) {
                val responseJson = String(responseTlvPacket.body)
                val responseJsonObject = responseJson.jsonToJsonObject()
                val action = responseJsonObject.optString("action")
                val success = responseJsonObject.optBoolean("success")
                if (action == "tryLock") {
                    if (success) {
                        logger.debug("receive:%s, cost:%s", responseJson, (System.currentTimeMillis() - begin))
                        true
                    } else {
                        println("-------------------need to waiting---------------------")
                        val a = it.receive { inputStream ->
                            this.tlvPacketProcessor.receiveTlvPacket(inputStream)
                        }
                        println(Date().toFormatString() + ", wait for:" + a?.type + "," + String(a?.body ?: ByteArray(0)))
                        false
                    }
                } else {
                    false
                }
            } else {
                false
            }
        }
//
        if (lockSuccess != null) {
            if (lockSuccess) {
                //do something
            } else {
                while (true) {

                }
            }
        }
    }

    fun releaseLock(lockKey: String) {
        this.socketClientPool.useSocketClient {
            val releaseLockRequestJson = releaseLockRequestJson(lockKey)
            val tlvPacket = TlvPacket(1.toByteArray(), releaseLockRequestJson.toByteArray())
            val begin = System.currentTimeMillis()
            val responseTlvPacket = it.send { outputStream, inputStream ->
                this.tlvPacketProcessor.sendTlvPacket(outputStream, tlvPacket)
                this.tlvPacketProcessor.receiveTlvPacket(inputStream)
            }
            if (responseTlvPacket != null) {
                val responseJson = String(responseTlvPacket.body)
                val responseJsonObject = responseJson.jsonToJsonObject()
                val action = responseJsonObject.optString("action")
                val success = responseJsonObject.optBoolean("success")
                if (action == "releaseLock") {
                    if (success) {
                        logger.debug("receive:%s, cost:%s", responseJson, (System.currentTimeMillis() - begin))
                        true
                    } else {
                        false
//                    val a = it.receive()
//                    println(String(a.body))
                    }
                } else {
                    false
                }
            }
        }
    }
}