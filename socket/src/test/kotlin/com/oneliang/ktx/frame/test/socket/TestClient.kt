package com.oneliang.ktx.frame.test.socket

import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val tlvPacketProcessor = TlvPacketProcessor()
    ClientManager("localhost", 9999, 1) {
        val tlvPacket = tlvPacketProcessor.receiveTlvPacket(it)
        println(String(tlvPacket.body))
    }.also { clientManager ->
        clientManager.start()
        clientManager.send(TlvPacket(1.toByteArray(), "i am client".toByteArray()).toByteArray())
    }
//    Client("localhost", 9999).start()
}