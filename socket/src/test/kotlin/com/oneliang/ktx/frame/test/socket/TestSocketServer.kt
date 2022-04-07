package com.oneliang.ktx.frame.test.socket

import com.oneliang.ktx.frame.socket.SocketServer
import com.oneliang.ktx.frame.socket.TlvPacket
import com.oneliang.ktx.frame.socket.TlvStreamProcessor
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toHexString
import java.io.InputStream
import java.io.OutputStream

fun main() {
    SocketServer(9999, true).also {
        it.streamProcessor = object : TlvStreamProcessor() {
            override fun process(inputStream: InputStream, outputStream: OutputStream) {
                println(Thread.currentThread().toString() + "server reading...")
                val buffer = ByteArray(1024)
                inputStream.read(buffer)
                println(buffer.toHexString())
                val tlvPacket = this.tlvPacketProcessor.receiveTlvPacket(inputStream)
                val requestString = String(tlvPacket.body)
                println(Thread.currentThread().toString() + ", server read:$requestString")
                this.tlvPacketProcessor.sendTlvPacket(outputStream, TlvPacket(1.toByteArray(), "server say:hello $requestString".toByteArray()))
            }
        }
        it.start()
    }
}