package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager
import java.nio.channels.Selector

class ClientManager(private val host: String, private val port: Int) : LoopThread() {
    companion object {
        private val logger = LoggerManager.getLogger(ClientManager::class)
    }

    var readProcessor: (byteArray: ByteArray) -> Unit = {}
    private val threadCount = Runtime.getRuntime().availableProcessors()
    private val threadPool = ThreadPool()
    private var clientArray: Array<Client> = emptyArray()

    @Throws(Throwable::class)
    override fun looping() {

    }

    @Synchronized
    override fun start() {
//thread pool
        this.threadPool.minThreads = 1
        this.threadPool.maxThreads = this.threadCount
        this.threadPool.start()
        this.clientArray = Array(this.threadCount) {
            Client(this.host, this.port, Selector.open()).also {
                it.readProcessor = this.readProcessor
            }
        }
        this.clientArray.forEach {
            this.threadPool.addThreadTask {
                it.run()
            }
        }
    }

    fun send(byteArray: ByteArray) {
        logger.info("aaa")
        if (this.clientArray.isEmpty()) {
            return
        }
        logger.info("bbb")
        val client = this.clientArray[byteArray.hashCode() % this.clientArray.size]
        client.send("123".toByteArray())
    }
}