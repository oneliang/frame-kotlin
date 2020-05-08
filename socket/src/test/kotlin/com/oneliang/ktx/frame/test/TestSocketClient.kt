package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.TcpPacketProcessor
import com.oneliang.ktx.frame.socket.SocketClientPool
import com.oneliang.ktx.frame.socket.SocketClientSource
import com.oneliang.ktx.frame.socket.TcpPacket
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toHexString
import java.net.Socket


fun main() {
    val socketClientPool = SocketClientPool()
    socketClientPool.resourceSource = SocketClientSource().also {
        it.host = "localhost"
        it.port = 9999
        it.tcpPacketProcessor = TcpPacketProcessor()
    }
    socketClientPool.initialize()
    object : Thread() {
        override fun run() {
            socketClientPool.useSocketClient {
                println(it)
                val tcpPacket = TcpPacket(1.toByteArray(), "111".toByteArray())
                for (i in 0..10) {
                    val begin = System.currentTimeMillis()
                    val responseTcpPacket = it.send(tcpPacket)
                    println(currentThread().toString() + ", receive:" + responseTcpPacket.toByteArray().toHexString() + ", cost:" + (System.currentTimeMillis() - begin))
                }
            }
        }
    }.start()
    object : Thread() {
        override fun run() {
            socketClientPool.useSocketClient {
                println(it)
                val tcpPacket = TcpPacket(2.toByteArray(), "222".toByteArray())
                for (i in 0..10) {
                    val begin = System.currentTimeMillis()
                    val responseTcpPacket = it.send(tcpPacket)
                    println(currentThread().toString() + ", receive:" + responseTcpPacket.toByteArray().toHexString() + ", cost:" + (System.currentTimeMillis() - begin))
                }
            }
        }
    }.start()
    Thread.sleep(5000)
    return
    val tcpPacketProcessor = TcpPacketProcessor()
    val client = Socket("127.0.0.1", 9999)
    val inputStream = client.getInputStream()
    val outputStream = client.getOutputStream()
    val tcpPacket = TcpPacket(1.toByteArray(), "123".toByteArray())
    println(tcpPacket.toByteArray().toHexString())
    tcpPacketProcessor.sendTcpPacket(outputStream, tcpPacket)
    while (true) {
        if (inputStream.available() > 0) {
            val byteArray = ByteArray(inputStream.available())
            inputStream.read(byteArray)
            println(String(byteArray))
            break
        }
    }
}