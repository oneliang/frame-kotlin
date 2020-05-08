package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.Socket
import java.util.concurrent.locks.ReentrantLock

class SocketClient(private val host: String, private val port: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(SocketClient::class)
    }

    private val socket = Socket(this.host, this.port)
    private val lock = ReentrantLock()

    lateinit var tcpPacketProcessor: TcpPacketProcessor

    /**
     * blocking method, when concurrent send
     */
    fun send(tcpPacket: TcpPacket): TcpPacket {
        this.lock.lock()
        return perform({
            val outputStream = this.socket.getOutputStream()
            val inputStream = this.socket.getInputStream()
            this.tcpPacketProcessor.sendTcpPacket(outputStream, tcpPacket)
            this.tcpPacketProcessor.receiveTcpPacket(inputStream)
        }, failure = {
            logger.error("send tcp package exception", it)
            TcpPacket()
        }, finally = {
            this.lock.unlock()
        })
    }

    fun close() {
        this.socket.close()
    }
}