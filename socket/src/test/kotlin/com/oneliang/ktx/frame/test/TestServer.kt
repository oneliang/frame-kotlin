package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.frame.socket.TcpPacket
import com.oneliang.ktx.frame.socket.TcpPacketProcessor
import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.SelectorThreadTask
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.concurrent.ThreadPool
import java.io.ByteArrayInputStream

fun main() {
    val tcpPacketProcessor = TcpPacketProcessor()
    val server = Server("localhost", 9999)
    server.threadPool = ThreadPool().also { it.start() }
    server.selectorProcessor = object : SelectorProcessor {
        override fun process(byteArray: ByteArray): ByteArray {
            val tcpPackage = tcpPacketProcessor.receiveTcpPacket(ByteArrayInputStream(byteArray))
            val requestString = String(tcpPackage.body)
            println(Thread.currentThread().toString() + ", server read :$requestString")
            return TcpPacket(1.toByteArray(), "server say: hello $requestString".toByteArray()).toByteArray()
        }
    }
    server.start()
}