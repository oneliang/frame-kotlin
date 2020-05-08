package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

class SocketServer(private val port: Int, private val longLink: Boolean = true) : LoopThread() {
    companion object {
        private val logger = LoggerManager.getLogger(SocketServer::class)
    }

    private val threadCount = Runtime.getRuntime().availableProcessors()
    private val threadPool = ThreadPool()
    private var serverSocket: ServerSocket? = null
    lateinit var streamProcessor: StreamProcessor
    private var longLinkCount = AtomicInteger(0)

    override fun run() {
        this.serverSocket = ServerSocket(this.port)
        super.run()
    }

    @Throws(Throwable::class)
    override fun looping() {
        val socket = serverSocket?.accept()
        socket ?: return
        this.threadPool.addThreadTask {
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()
            perform({
                do {
                    logger.info("socket:%s ,processing...", socket)
                    this.streamProcessor.process(inputStream, outputStream)
                } while (this.longLink)
            }, failure = { it ->
                logger.error("socket processor exception", it)
                throw it
            }, finally = {
                if (!this.longLink) {
                    socket.close()
                }
                if (this.longLink) {
                    this.longLinkCount.decrementAndGet()
                }
                logger.info("long link task finished. long link count:%s", this.longLinkCount.get())
            })
        }
        if (this.longLink) {
            this.longLinkCount.getAndIncrement()
        }
        logger.info("long link count:%s", this.longLinkCount.get())
    }

    @Synchronized
    override fun start() {
        super.start()
        //thread pool
        this.threadPool.minThreads = 1
        this.threadPool.maxThreads = threadCount
        this.threadPool.start()
    }
}