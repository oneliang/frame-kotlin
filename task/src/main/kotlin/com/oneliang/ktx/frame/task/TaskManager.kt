package com.oneliang.ktx.frame.task

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class TaskManager(maxThreads: Int=1) {
    companion object {
        private val logger = LoggerManager.getLogger(TaskManager::class)
    }

    private val executorService = Executors.newFixedThreadPool(maxThreads)
    private val coroutine = Coroutine(executorService.asCoroutineDispatcher())
    private val taskJobMap = ConcurrentHashMap<Int, Pair<Task, Job>>()
    private val cancelledTaskMap = ConcurrentHashMap<Int, Task>()

    /**
     * task definition
     */
    abstract class Task {
        @Volatile
        internal var running = false

        internal suspend fun internalRun() {
            this.running = true
            run()
            this.running = false
        }

        /**
         * run
         */
        abstract suspend fun run()

        /**
         * cancel callback
         */
        open suspend fun cancelCallback() {
        }
    }

    /**
     * launch
     */
    fun startTask(
        task: Task,
        priority: Boolean = false
    ): Job {
        if (priority) {
            this.cancelAllTask()
        }
        val hashCode = task.hashCode()
        val job = this.coroutine.launch {
            try {
                task.internalRun()
            } catch (e: Throwable) {
                try {
                    task.cancelCallback()
                } catch (e: Throwable) {
                    logger.error("cancelCallback exception, task hashCode:%s", e, hashCode)
                } finally {
                    this.cancelledTaskMap[hashCode] = task
                }
                logger.error("cancelling coroutine job, task hashCode:%s", e, hashCode)
            } finally {
                this.taskJobMap.remove(task.hashCode())
                if (priority) {
                    this.restoreAllCancelledTask()
                }
            }
        }
        this.taskJobMap[hashCode] = task to job
        return job
    }

    /**
     * cancel all task
     */
    fun cancelAllTask() {
        logger.debug("cancel all task, task job map size:%s".format(this.taskJobMap.size))
        this.taskJobMap.forEach { (key, taskJob) ->
            val (task, job) = taskJob
            if (!task.running) {
                this.cancelledTaskMap[task.hashCode()] = task
            }
            job.cancel()
        }
        logger.debug("cancel all task, cancelled task map size:%s".format(this.cancelledTaskMap.size))
    }

    /**
     * restore all cancelled task
     */
    fun restoreAllCancelledTask() {
        logger.debug("restore all cancelled task, cancelled task map size:%s".format(this.cancelledTaskMap.size))
        this.cancelledTaskMap.forEach { (_, task) ->
            this.startTask(task)
        }
    }
}