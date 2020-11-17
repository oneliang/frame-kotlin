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
        this.threadPool.minThreads = minThreads
        this.threadPool.maxThreads = maxThreads
    }

    private val timer = Timer()

    fun start() {
        this.threadPool.start()
    }

    fun interrupt() {
        this.threadPool.interrupt()
    }

    fun addTimerTask(startDate: Date, intervalTime: Long, task: (threadPool: ThreadPool) -> Unit) {
        val timerTask = object : TimerTask() {
            override fun run() {
                threadPool.addThreadTask({
                    val begin = System.currentTimeMillis()
                    task(threadPool)
                    logger.info("task finished， cost:%s", (System.currentTimeMillis() - begin))
                }, failure = {
                    logger.error("execute task error", it)
                })
            }
        }
        this.timer.schedule(timerTask, startDate, intervalTime)
    }
}