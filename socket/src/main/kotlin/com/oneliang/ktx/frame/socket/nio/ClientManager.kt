package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager
import java.nio.channels.Selector
import java.util.concurrent.locks.ReentrantLock

class ClientManager(
    private val serverHost: String,
    private val serverPort: Int,
    private val threadCount: Int = Runtime.getRuntime().availableProcessors(),
    private val readProcessor: (byteArray: ByteArray) -> Unit = {},
    private val clientStatusCallback: ClientStatusCallback? = null
) {
    companion object {
        private val logger = LoggerManager.getLogger(ClientManager::class)
    }

    @Volatile
    private var hasBeenInitialized = false
    private val initializeLock = ReentrantLock()
    private val threadPool = ThreadPool()
    private var clients: Array<Client> = emptyArray()

    private fun initialize() {
        if (this.hasBeenInitialized) {
            return
        }
        try {
            this.initializeLock.lock()
            if (this.hasBeenInitialized) {//double check
                return//return will trigger finally, but use unlock safety
            }
            this.threadPool.minThreads = 1
            this.threadPool.maxThreads = if (this.threadCount < 1) Runtime.getRuntime().availableProcessors() else this.threadCount
            this.threadPool.start()
            this.clients = Array(this.threadCount) { clientIndex ->
                Client(this.serverHost, this.serverPort, Selector.open(), this.readProcessor, object : Client.StatusCallback {
                    override fun onConnect() {
                        this@ClientManager.clientStatusCallback?.onConnect(clientIndex)
                    }

                    override fun onDisconnect() {
                        this@ClientManager.clientStatusCallback?.onDisconnect(clientIndex)
                    }
                })
            }
            this.clients.forEach {
                this.threadPool.addThreadTask {
                    it.run()
                }
            }
            this.hasBeenInitialized = true
        } catch (t: Throwable) {
            this.hasBeenInitialized = false
            logger.error(Constants.String.EXCEPTION, t)
        } finally {
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
        for (client in this.clients) {
            client.close()
        }
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

    /**
     * ClientStatusCallback
     */
    interface ClientStatusCallback {

        /**
         * on connect, blocking callback
         * @param clientIndex
         */
        fun onConnect(clientIndex: Int)

        /**
         * on disconnect, blocking callback
         * @param clientIndex
         */
        fun onDisconnect(clientIndex: Int)
    }
}