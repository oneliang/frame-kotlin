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
    private var clients: Array<Client> = emptyArray()

    @Throws(Throwable::class)
    override fun looping() {

    }

    @Synchronized
    override fun start() {
//thread pool
        this.threadPool.minThreads = 1
        this.threadPool.maxThreads = this.threadCount
        this.threadPool.start()
        this.clients = Array(this.threadCount) {
            Client(this.host, this.port, Selector.open()).also {
                it.readProcessor = this.readProcessor
            }
        }
        this.clients.forEach {
            this.threadPool.addThreadTask {
                it.run()
            }
        }
    }

    fun send(byteArray: ByteArray) {
        if (this.clients.isEmpty()) {
            return
        }
        val client = this.clients[byteArray.hashCode() % this.clients.size]
        client.send(byteArray)
    }
}