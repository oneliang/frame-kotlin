package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager
import java.nio.channels.Selector
import java.util.concurrent.locks.ReentrantLock

class ClientManager(
    private val host: String,
    private val port: Int,
    private val readProcessor: (byteArray: ByteArray) -> Unit = {}
) {
    companion object {
        private val logger = LoggerManager.getLogger(ClientManager::class)
    }

    @Volatile
    private var hasBeenInitialized = false
    private val initializeLock = ReentrantLock()
    private val threadCount = 1//Runtime.getRuntime().availableProcessors()
    private val threadPool = ThreadPool()
    private var clients: Array<Client> = emptyArray()


    private fun initialize() {
        if (this.hasBeenInitialized) {
            return
        }
        try {
            this.initializeLock.lock()
            this.threadPool.minThreads = 1
            this.threadPool.maxThreads = this.threadCount
            this.threadPool.start()
            this.clients = Array(this.threadCount) {
                Client(this.host, this.port, Selector.open(), this.readProcessor)
            }
            this.clients.forEach {
                this.threadPool.addThreadTask {
                    it.run()
                }
            }
        } finally {
            this.hasBeenInitialized = true
            this.initializeLock.unlock()
        }
    }

    @Synchronized
    fun start() {
        if (!this.hasBeenInitialized) {
            this.initialize()
        }
    }

    @Synchronized
    fun stop() {
        this.threadPool.stop()
        this.clients = emptyArray()
        this.hasBeenInitialized = false
    }

    fun send(byteArray: ByteArray) {
        if (!this.hasBeenInitialized) {
            this.initialize()
        }
        if (this.clients.isEmpty()) {
            return
        }
        val client = this.clients[byteArray.hashCode() % this.clients.size]
        client.send(byteArray)
    }
}