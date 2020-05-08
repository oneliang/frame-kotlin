package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.util.*

class Server(private val host: String, private val port: Int) : LoopThread() {
    companion object {
        private val logger = LoggerManager.getLogger(Server::class)
    }

    lateinit var selectorProcessor: SelectorProcessor
    lateinit var threadPool: ThreadPool
    private val acceptSelector: Selector = Selector.open()
    private var selectorThreadTask: Array<SelectorThreadTask> = emptyArray()
    private var serverSocketChannel: ServerSocketChannel? = null

    override fun run() {
        perform({
            this.serverSocketChannel = ServerSocketChannel.open()
            this.serverSocketChannel?.configureBlocking(false)
            this.serverSocketChannel?.bind(InetSocketAddress(host, port))
            this.serverSocketChannel?.register(this.acceptSelector, SelectionKey.OP_ACCEPT)
            logger.debug("server start")
            super.run()
        }) {
            logger.error("server exception，shutdown", it)
        }
    }

    @Throws(Throwable::class)
    override fun looping() {
        val serverSocketChannel = this.serverSocketChannel ?: return
        logger.debug("server select, is open:%s", serverSocketChannel.isOpen)
        this.acceptSelector.select()
        val keysIterator = acceptSelector.selectedKeys().iterator()
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            keysIterator.remove()
            if (key.isAcceptable) {
                val socketChannel = serverSocketChannel.accept()
                socketChannel.configureBlocking(false)
                logger.info("begin:%s", key)
                val selector = this.selectorThreadTask[socketChannel.hashCode() % this.selectorThreadTask.size].selector
                logger.info("end:%s, selector:%s", key, selector)
                selector.wakeup()//OP_READ will block select(), so wakeup first and read the data
                socketChannel.register(selector, SelectionKey.OP_READ)
                val socketAddress = socketChannel.remoteAddress as InetSocketAddress
                logger.debug("client connected, time:%s, host:%s, port:%s", Date(), socketAddress.hostString, socketAddress.port)
            }
        }
    }

    /**
     * start
     */
    @Synchronized
    override fun start() {
        super.start()
        //thread pool
//        this.threadPool.start()
        this.selectorThreadTask = Array(this.threadPool.maxThreads) {
            SelectorThreadTask(Selector.open()).also {
                it.selectorProcessor = this.selectorProcessor
            }
        }
        this.selectorThreadTask.forEach {
            this.threadPool.addThreadTask(it)
        }
    }

    /**
     * interrupt
     */
    override fun interrupt() {
        super.interrupt()
        this.acceptSelector.wakeup()
        this.threadPool.interrupt()
        this.selectorThreadTask = emptyArray()
    }
}