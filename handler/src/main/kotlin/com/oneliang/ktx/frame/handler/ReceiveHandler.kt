package com.oneliang.ktx.frame.handler

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager

class ReceiveHandler<T : Any>(
    private val threadCount: Int = 1,
    private val initialize: () -> T,
    private val loopingProcess: (T) -> ((T) -> Unit)
) : LoopThread() {
    companion object {
        private val logger = LoggerManager.getLogger(ReceiveHandler::class)
    }

    private val threadPool = ThreadPool()
    private lateinit var resource: T

    @Synchronized
    override fun start() {
        if (!this::resource.isInitialized) {
            this.resource = this.initialize()
        }
        this.threadPool.minThreads = 1
        this.threadPool.maxThreads = this.threadCount
        this.threadPool.start()
        super.start()
    }

    override fun looping() {
        val task = this.loopingProcess(this.resource)
        execute(task)
    }

    fun execute(task: (T) -> Unit) {
        this.threadPool.addThreadTask({
            task.invoke(this.resource)
        }, failure = {
            logger.error(Constants.Base.EXCEPTION, it)
        })
    }

    @Synchronized
    override fun interrupt() {
        this.threadPool.interrupt()
        super.interrupt()
    }
}