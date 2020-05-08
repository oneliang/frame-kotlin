package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.socket.SocketServer
import com.oneliang.ktx.frame.socket.TcpPacket
import com.oneliang.ktx.frame.socket.TcpStreamProcessor
import com.oneliang.ktx.util.common.toByteArray
import java.io.InputStream
import java.io.OutputStream

fun main() {
    SocketServer(9999, true).also {
        it.streamProcessor = object : TcpStreamProcessor() {
            override fun process(inputStream: InputStream, outputStream: OutputStream) {
                println(Thread.currentThread().toString() + "server reading...")
                val tcpPackage = this.tcpPacketProcessor.receiveTcpPacket(inputStream)
                val requestString = String(tcpPackage.body)
                println(Thread.currentThread().toString() + ", server read:$requestString")
                this.tcpPacketProcessor.sendTcpPacket(outputStream, TcpPacket(1.toByteArray(), "server say:hello $requestString".toByteArray()))
            }
        }
        it.start()
    }
}