package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Server(
    private val host: String, private val port: Int,
    private val maxThreadCount: Int = Runtime.getRuntime().availableProcessors()
) : LoopThread() {
    companion object {
        private val logger = LoggerManager.getLogger(Server::class)
    }

    lateinit var selectorProcessor: SelectorProcessor
    private val threadPool: ThreadPool = ThreadPool()
    private val acceptSelector: Selector = Selector.open()
    private var selectorThreadTask: Array<SelectorThreadTask> = emptyArray()
    private var serverSocketChannel: ServerSocketChannel? = null
    private val socketChannelMap = ConcurrentHashMap<Int, SocketChannel>()

    override fun run() {
        try {
            this.serverSocketChannel = ServerSocketChannel.open()
            this.serverSocketChannel?.configureBlocking(false)
            this.serverSocketChannel?.bind(InetSocketAddress(this.host, this.port))
            this.serverSocketChannel?.register(this.acceptSelector, SelectionKey.OP_ACCEPT)
            logger.debug("server start")
            super.run()
        } catch (e: Throwable) {
            logger.error("server exception, shutdown", e)
        }
    }

    @Throws(Throwable::class)
    override fun looping() {
        val serverSocketChannel = this.serverSocketChannel ?: return
        logger.debug("server select, is open:%s", serverSocketChannel.isOpen)
        this.acceptSelector.select()//blocking
        val keysIterator = this.acceptSelector.selectedKeys().iterator()
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            keysIterator.remove()
            if (key.isAcceptable) {
                val socketChannel = serverSocketChannel.accept()
                val socketChannelHashCode = socketChannel.hashCode()
                this.socketChannelMap[socketChannelHashCode] = socketChannel
                logger.debug("current socket channel map size:%s", this.socketChannelMap.size)
                socketChannel.configureBlocking(false)
                val selector = getSelector(socketChannelHashCode)
                logger.debug("socket channel:%s, selector:%s", socketChannel, selector)
                selector.wakeup()//OP_READ will block select(), so wakeup first and read the data
                socketChannel.register(selector, SelectionKey.OP_READ)
                val socketAddress = socketChannel.remoteAddress as InetSocketAddress
                logger.debug("client connected, time:%s, host:%s, port:%s", Date(), socketAddress.hostString, socketAddress.port)
            }
        }
    }

    /**
     * get selector
     */
    private fun getSelector(socketChannelHashCode: Int): Selector {
        val selectorThreadTaskIndex = socketChannelHashCode % this.selectorThreadTask.size
        return selectorThreadTask[selectorThreadTaskIndex].selector
    }

    /**
     * start
     */
    @Synchronized
    override fun start() {
        super.start()
        //thread pool
        this.threadPool.maxThreads = this.maxThreadCount
        this.threadPool.start()
        this.selectorThreadTask = Array(this.threadPool.maxThreads) {
            SelectorThreadTask(Selector.open()) { socketChannel ->
                this.socketChannelMap.remove(socketChannel)
                logger.debug("disconnect, current socket channel map:%s", this.socketChannelMap.toJson())
            }.also {
                it.selectorProcessor = this.selectorProcessor
            }
        }
        this.selectorThreadTask.forEach {
            this.threadPool.addThreadTask(it)
        }
    }

    /**
     * stop interrupt
     */
    @Synchronized
    override fun stop() {
        super.stop()
        this.acceptSelector.wakeup()
        this.threadPool.stop()
        this.selectorThreadTask = emptyArray()
        this.serverSocketChannel = null
        this.socketChannelMap.clear()
    }

    fun notify(socketChannelHashCode: Int, byteArray: ByteArray = ByteArray(0)): Boolean {
        val socketChannel = this.socketChannelMap[socketChannelHashCode]
        return if (socketChannel != null) {
            val selector = getSelector(socketChannelHashCode)
            socketChannel.register(selector, SelectionKey.OP_WRITE, byteArray)
            selector.wakeup()
            true
        } else {
            logger.warning("socket channel does not exists, socketChannelHashCode:%s", socketChannelHashCode)
            false
        }
    }

    fun notify(socketChannelHashCodes: Collection<Int>) {
        socketChannelHashCodes.forEach { socketChannelHashCode ->
            notify(socketChannelHashCode)
        }
    }
}