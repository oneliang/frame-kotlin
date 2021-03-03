package com.oneliang.ktx.frame.handler

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager

class SendHandler<T : Any>(
    private val threadCount: Int = 1,
    private val initialize: () -> T
) {
    companion object {
        private val logger = LoggerManager.getLogger(SendHandler::class)
    }

    private val threadPool = ThreadPool()
    private lateinit var resource: T

    @Synchronized
    fun start() {
        if (!this::resource.isInitialized) {
            this.resource = this.initialize()
        }
        this.threadPool.minThreads = 1
        this.threadPool.maxThreads = this.threadCount
        this.threadPool.start()
    }

    fun execute(handle: (T) -> Unit) {
        this.threadPool.addThreadTask({
            handle(this.resource)
        }, failure = {
            logger.error(Constants.String.EXCEPTION, it)
        })
    }

    @Synchronized
    fun interrupt() {
        this.threadPool.interrupt()
    }
}