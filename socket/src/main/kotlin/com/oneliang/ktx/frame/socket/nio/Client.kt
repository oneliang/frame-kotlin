package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class Client(private val host: String, private val port: Int, private val selector: Selector) {
    companion object {
        private val logger = LoggerManager.getLogger(Client::class)
    }

    internal var readProcessor: (byteArray: ByteArray) -> Unit = {}
    private var socketChannel: SocketChannel? = null

    fun send(byteArray: ByteArray) {
        val socketChannel = this.socketChannel ?: return
        if (!socketChannel.isOpen) {
            logger.error("socket channel is not open")
            return
        }
        socketChannel.write(ByteBuffer.wrap(byteArray))
        this.selector.wakeup()
        socketChannel.register(this.selector, SelectionKey.OP_READ)
        this.selector.wakeup()
    }

    fun run() {
        val socketChannel = SocketChannel.open()
        this.socketChannel = socketChannel ?: return
        socketChannel.configureBlocking(false)
        socketChannel.register(this.selector, SelectionKey.OP_CONNECT)
        socketChannel.connect(InetSocketAddress(this.host, this.port))
        perform({
            while (true) {
                logger.debug("client, is open:%s", socketChannel.isOpen)
                if (socketChannel.isOpen) {
                    this.selector.select()
                    val keysIterator = this.selector.selectedKeys().iterator()
                    while (keysIterator.hasNext()) {
                        val key = keysIterator.next()
                        keysIterator.remove()
                        when {
                            key.isConnectable -> {
                                while (!socketChannel.finishConnect()) {
                                    logger.debug("client:%s, connecting", this)
                                }
                                socketChannel.register(this.selector, SelectionKey.OP_READ)
                            }
                            key.isReadable -> {
                                perform({
                                    val byteArray = socketChannel.readByteArray()
                                    this.readProcessor(byteArray)
                                    this.selector.wakeup()
                                    socketChannel.register(this.selector, SelectionKey.OP_READ)
                                    this.selector.wakeup()
                                    logger.debug("byte array md5:%s, byte array size:%s", byteArray.MD5String(), byteArray.size)
                                }) {
                                    logger.debug("server error.")
                                    key.cancel()
                                    socketChannel.close()
                                }
                            }
                        }
                    }
                } else {
                    break
                }
            }
        }) {
            logger.error("client exception, please restart", it)
        }
    }
}