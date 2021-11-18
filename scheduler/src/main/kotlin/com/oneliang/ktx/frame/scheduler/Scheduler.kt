package com.oneliang.ktx.frame.scheduler

import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*

class Scheduler(minThreads: Int, maxThreads: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(Scheduler::class)
    }

    private var threadPool = ThreadPool()

    init {
        if (minThreads <= 0 || maxThreads < minThreads) {
            error("param(minThreads) must be lager than 0, and param(maxThreads) must be lager than or equal param(minThreads)")
        }
        this.threadPool.minThreads = minThreads
        this.threadPool.maxThreads = maxThreads
    }

    private var timer: Timer? = null

    fun start() {
        this.threadPool.start()
        this.timer = Timer()
    }

    fun interrupt() {
        this.timer?.cancel()
        this.timer = null
        this.threadPool.interrupt()
    }

    fun addTimerTask(startDate: Date, intervalTime: Long, task: (threadPool: ThreadPool) -> Unit) {
        val timerTask = object : TimerTask() {
            override fun run() {
                threadPool.addThreadTask({
                    val begin = System.currentTimeMillis()
                    task(threadPool)
                    logger.info("task finished, cost:%s", (System.currentTimeMillis() - begin))
                }, failure = {
                    logger.error("execute task error", it)
                })
            }
        }
        this.timer?.schedule(timerTask, startDate, intervalTime)
    }
}