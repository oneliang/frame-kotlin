package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.concurrent.atomic.OperationLock
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class SocketClient(private val host: String, private val port: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(SocketClient::class)
    }

    private val socket = Socket(this.host, this.port)
    private val operationLock = OperationLock()

    fun <R> receive(receiveBlock: (inputStream: InputStream) -> R): R? {
        return try {
            val inputStream = this.socket.getInputStream()
            receiveBlock(inputStream)
        } catch (e: Throwable) {
            logger.error("receive tlv package exception", e)
            null
        }
    }

    /**
     * blocking method, when concurrent send
     */
    fun <R> send(sendBlock: (outputStream: OutputStream, inputStream: InputStream) -> R): R? {
        return try {
            this.operationLock.operate {
                val outputStream = this.socket.getOutputStream()
                val inputStream = this.socket.getInputStream()
                sendBlock(outputStream, inputStream)
            }
        } catch (e: Throwable) {
            logger.error("send tlv package exception", e)
            null
        }
    }

    fun close() {
        this.socket.close()
    }
}