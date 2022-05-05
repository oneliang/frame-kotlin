package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock

class Client(
    private val host: String,
    private val port: Int,
    private val selector: Selector,
    private val readProcessor: (byteArray: ByteArray) -> Unit = {}
) {
    companion object {
        private val logger = LoggerManager.getLogger(Client::class)
    }

    @Volatile
    private var hasBeenInitialized = false
    private val initializeLock = ReentrantLock()
    private var socketChannel: SocketChannel? = null
    private val sendQueue = ConcurrentLinkedQueue<ByteArray>()
    private val reconnectTimeout = 5000L

    init {
        initialize()
    }

    private fun initialize() {
        if (this.hasBeenInitialized) {
            return
        }
        try {
            this.initializeLock.lock()
            if (this.hasBeenInitialized) {//double check
                return//return will trigger finally, but use unlock safety
            }
            val socketChannel = SocketChannel.open()
            this.socketChannel = socketChannel ?: error("socketChannel is null")
            socketChannel.configureBlocking(false)
            socketChannel.connect(InetSocketAddress(this.host, this.port))
            socketChannel.register(this.selector, SelectionKey.OP_CONNECT)
            this.hasBeenInitialized = true
        } catch (t: Throwable) {
            this.hasBeenInitialized = false
            logger.error(Constants.String.EXCEPTION, t)
        } finally {
            this.initializeLock.unlock()
        }
    }

    private fun resetSocketChannel() {
        this.socketChannel = null
        this.hasBeenInitialized = false
    }

    fun send(byteArray: ByteArray) {
        if (!this.hasBeenInitialized) {
            initialize()
        }
        this.sendQueue.add(byteArray)//support send before run
        val socketChannel = this.socketChannel
        if (socketChannel != null && socketChannel.isConnected) {//send after connected
            socketChannel.register(this.selector, SelectionKey.OP_WRITE)
            this.selector.wakeup()
        }
    }

    fun run() {
        if (!this.hasBeenInitialized) {
            initialize()
        }
        while (true) {
            val privateSocketChannel = this.socketChannel
            if (privateSocketChannel != null && privateSocketChannel.isOpen) {
                this.selector.select()//blocking
                val keysIterator = this.selector.selectedKeys().iterator()
                logger.verbose("before selected key size:%s, send queue size:%s", this.selector.selectedKeys().size, this.sendQueue.size)
                while (keysIterator.hasNext()) {
                    val key = keysIterator.next()
                    keysIterator.remove()
                    logger.debug("client:%s, isReadable:%s, isWritable:%s", this, key.isReadable, key.isWritable)
                    val socketChannel = key.channel() as SocketChannel
                    val address = socketChannel.remoteAddress as InetSocketAddress
                    try {
                        when {
                            key.isConnectable -> {
                                while (!socketChannel.finishConnect()) {
                                    logger.debug("client:%s, connecting", this)
                                }
                                logger.debug("client connected, socket channel:%s", socketChannel)
                                key.interestOps(SelectionKey.OP_READ or SelectionKey.OP_WRITE)
//                                socketChannel.register(this.selector, SelectionKey.OP_READ or SelectionKey.OP_WRITE)
                            }
                            key.isReadable -> {
                                logger.debug("isReadable, time:%s, host:%s, port:%s", Date(), address.hostString, address.port)
                                val byteArray = socketChannel.readByteArray()
                                logger.debug("client read, byte array md5:%s, byte array size:%s", byteArray.MD5String(), byteArray.size)
                                this.readProcessor(byteArray)
                                key.interestOps(key.interestOps() or SelectionKey.OP_READ)
//                                socketChannel.register(this.selector, SelectionKey.OP_READ)
                                this.selector.wakeup()
                            }
                            key.isWritable -> {
                                logger.debug("isWritable, time:%s, host:%s, port:%s, send queue size:%s", Date(), address.hostString, address.port, this.sendQueue.size)
                                while (this.sendQueue.isNotEmpty()) {
                                    val byteArray = this.sendQueue.poll()
                                    socketChannel.write(ByteBuffer.wrap(byteArray))
                                    logger.debug("client write, byte array md5:%s, byte array size:%s", byteArray.MD5String(), byteArray.size)
                                }
                                key.interestOps(key.interestOps() and SelectionKey.OP_WRITE.inv() or SelectionKey.OP_READ)//remove the write key from interest ops
//                                socketChannel.register(this.selector, SelectionKey.OP_READ)
                            }
                        }
                    } catch (e: Throwable) {
                        logger.error("client exception. cancel key and close socket channel", e)
                        key.cancel()
                        socketChannel.close()
                        this.resetSocketChannel()
                    }
                }
                logger.verbose("after selected key size:%s", this.selector.selectedKeys().size)
            } else if (privateSocketChannel == null) {
                logger.debug("socket channel is null, maybe reconnecting, maybe server is close, host:%s, port:%s", this.host, this.port)
                this.hasBeenInitialized = false
                this.initialize()//reset socket channel
                Thread.sleep(this.reconnectTimeout)
            }//else is not open
        }
    }
}