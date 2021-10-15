package com.oneliang.ktx.frame.test.socket

import com.oneliang.ktx.frame.socket.TcpPacket
import com.oneliang.ktx.frame.socket.TcpPacketProcessor
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.toByteArray

fun main() {
    val tcpPacketProcessor = TcpPacketProcessor()
    ClientManager("localhost", 9999).also { clientManager ->
        clientManager.readProcessor = {
            val tcpPacket = tcpPacketProcessor.receiveTcpPacket(it)
            println(String(tcpPacket.body))
        }
        clientManager.start()
        Thread.sleep(1000)
        clientManager.send(TcpPacket(1.toByteArray(), "i am client".toByteArray()).toByteArray())
    }
//    Client("localhost", 9999).start()
}