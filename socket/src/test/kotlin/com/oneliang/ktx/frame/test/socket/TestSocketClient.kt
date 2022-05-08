package com.oneliang.ktx.frame.test.socket

import com.oneliang.ktx.frame.socket.SocketClientPool
import com.oneliang.ktx.frame.socket.SocketClientSource
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toHexString
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.net.Socket

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val tlvPacketProcessor = TlvPacketProcessor()
    val socketClientPool = SocketClientPool()
    socketClientPool.setResourceSource(SocketClientSource().also {
        it.host = "localhost"
        it.port = 9999
    })
    socketClientPool.initialize()
    object : Thread() {
        override fun run() {
            socketClientPool.useSocketClient {
                println(it)
                val tlvPacket = TlvPacket(1.toByteArray(), "111".toByteArray())
                for (i in 1..10) {
                    val begin = System.currentTimeMillis()
                    val responseTlvPacket = it.send { outputStream, inputStream ->
                        tlvPacketProcessor.sendTlvPacket(outputStream, tlvPacket)
                        tlvPacketProcessor.receiveTlvPacket(inputStream)
                    }
                    if (responseTlvPacket != null) {
                        println(currentThread().toString() + ", receive:" + responseTlvPacket.toByteArray().toHexString() + ", cost:" + (System.currentTimeMillis() - begin))
                        val receiveTlvPacket = it.receive { inputStream ->
                            tlvPacketProcessor.receiveTlvPacket(inputStream)
                        }
                        println(currentThread().toString() + ", receive2:" + String(receiveTlvPacket?.body ?: ByteArray(0)) + ", cost:" + (System.currentTimeMillis() - begin))
                    }
                }
            }
        }
    }.start()
//    object : Thread() {
//        override fun run() {
//            socketClientPool.useSocketClient {
//                println(it)
//                val tlvPacket = TlvPacket(2.toByteArray(), "222".toByteArray())
//                for (i in 0..10) {
//                    val begin = System.currentTimeMillis()
//                    val responseTlvPacket = it.send(tlvPacket)
//                    println(currentThread().toString() + ", receive:" + responseTlvPacket.toByteArray().toHexString() + ", cost:" + (System.currentTimeMillis() - begin))
//                }
//            }
//        }
//    }.start()
    Thread.sleep(5000)
    return
    val client = Socket("127.0.0.1", 9999)
    val inputStream = client.getInputStream()
    val outputStream = client.getOutputStream()
    val tlvPacket = TlvPacket(1.toByteArray(), "123".toByteArray())
    println(tlvPacket.toByteArray().toHexString())
    tlvPacketProcessor.sendTlvPacket(outputStream, tlvPacket)
    while (true) {
        if (inputStream.available() > 0) {
            val byteArray = ByteArray(inputStream.available())
            inputStream.read(byteArray)
            println(String(byteArray))
            break
        }
    }
}