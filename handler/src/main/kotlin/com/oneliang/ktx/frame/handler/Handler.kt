package com.oneliang.ktx.frame.handler

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager

class Handler<T : Any>(
    private val threadCount: Int = 1,
    private val initialize: () -> T,
    private val loopingProcess: (T) -> Runnable
) : LoopThread() {
    companion object {
        private val logger = LoggerManager.getLogger(Handler::class)
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
        val runnable = this.loopingProcess(this.resource)
        execute(runnable)
    }

    fun execute(runnable: Runnable) {
        this.threadPool.addThreadTask({
            runnable.run()
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