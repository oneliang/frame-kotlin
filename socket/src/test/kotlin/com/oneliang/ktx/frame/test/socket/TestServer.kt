package com.oneliang.ktx.frame.test.socket

import com.oneliang.ktx.frame.socket.TlvPacket
import com.oneliang.ktx.frame.socket.TlvPacketProcessor
import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.ByteArrayInputStream

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val tlvPacketProcessor = TlvPacketProcessor()
    val server = Server("localhost", 9999)
    server.selectorProcessor = object : SelectorProcessor {
        override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
            val tlvPacket = tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
            val requestString = String(tlvPacket.body)
            println(Thread.currentThread().toString() + ", server read :$requestString")
            return TlvPacket(1.toByteArray(), "server say: hello client, i am server, did you said:$requestString".toByteArray()).toByteArray()
        }
    }
    server.start()
}