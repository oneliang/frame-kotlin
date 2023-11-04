package com.oneliang.ktx.frame.task

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class TaskManager(maxThreads: Int = 1) {
    companion object {
        private val logger = LoggerManager.getLogger(TaskManager::class)
    }

    private val executorService = Executors.newFixedThreadPool(maxThreads)
    private val coroutine = Coroutine(executorService.asCoroutineDispatcher())
    private val taskMap = ConcurrentHashMap<Int, Task>()
    private val cancelledTaskMap = ConcurrentHashMap<Int, Task>()

    /**
     * task definition
     */
    abstract class Task(val maxProcessors: Int = 1) {

        internal val processorJobMap = ConcurrentHashMap<Int, Pair<Processor, Job>>()
        private val cancelledProcessorMap = ConcurrentHashMap<Int, Processor>()
        private val runningProcessorCount = AtomicInteger(this.maxProcessors)

        init {
            if (this.maxProcessors <= 0) {
                error("param[maxProcessors] must be large than 0")
            }
        }


        /**
         * internal run processor
         * @param processor
         * @param processorIndex
         * @param taskFinishedCallback
         */
        internal suspend fun internalRunProcessor(
            processor: Processor,
            processorIndex: Int,
            taskFinishedCallback: suspend (taskHashCode: Int) -> Unit
        ) {
            processor.running = true
            try {
                this.runProcessor(processorIndex)
            } catch (e: Throwable) {
                try {
                    this.onCanceledProcessor(processorIndex)
                } catch (e: Throwable) {
                    logger.error("cancelCallback exception, processor:%s", e, processor)
                } finally {
                    this.cancelledProcessorMap[processorIndex] = processor
                }
                logger.error("cancelling coroutine job, processor:%s", e, processor)
            } finally {
                this.processorJobMap.remove(processorIndex)
                processor.running = false
                val runningCount = this.runningProcessorCount.decrementAndGet()
                if (runningCount <= 0) {
                    taskFinishedCallback(this.hashCode())
                    try {
                        onFinished()
                    } catch (e: Throwable) {
                        logger.error("finished execute exception, task:%s", e, this)
                    }
                }

            }
        }

        /**
         * run processor
         */
        abstract suspend fun runProcessor(processorIndex: Int)

        /**
         * on canceled processor
         */
        open suspend fun onCanceledProcessor(processorIndex: Int) {
        }

        /**
         * on finished
         */
        open suspend fun onFinished() {
        }

        /**
         * cancel all processor
         */
        fun cancelAllProcessor() {
            logger.debug("cancel all task, task job map size:%s".format(this.processorJobMap.size))
            this.processorJobMap.forEach { (key, processorJob) ->
                val (processor, job) = processorJob
                if (!processor.running) {
                    this.cancelledProcessorMap[processor.hashCode()] = processor
                }
                job.cancel()
            }
            logger.debug("cancel all task, cancelled task map size:%s".format(this.cancelledProcessorMap.size))
        }


        /**
         * processor definition
         */
        class Processor {
            @Volatile
            internal var running = false
        }

    }

    /**
     * launch
     */
    fun startTask(
        task: Task,
        priority: Boolean = false
    ) {
        if (priority) {
            this.cancelAllTask()
        }
        val hashCode = task.hashCode()
        for (processorIndex in 0 until task.maxProcessors) {
            val processor = Task.Processor()
            val job = this.coroutine.launch {
                task.internalRunProcessor(processor, processorIndex) {
                    this.taskMap.remove(hashCode)
                }
            }
            task.processorJobMap[processorIndex] = processor to job
        }
        this.taskMap[hashCode] = task
//        return job
    }

    /**
     * cancel all task
     */
    fun cancelAllTask() {
        logger.debug("cancel all task, task job map size:%s".format(this.taskMap.size))
        this.taskMap.forEach { (hashCode, task) ->
            task.cancelAllProcessor()
            this.taskMap.remove(hashCode)
            this.cancelledTaskMap[hashCode] = task
        }
        logger.debug("cancel all task, cancelled task map size:%s".format(this.cancelledTaskMap.size))
    }

    /**
     * restore all cancelled task
     */
    fun restoreAllCancelledTask() {
        logger.debug("restore all cancelled task, cancelled task map size:%s".format(this.cancelledTaskMap.size))
        this.cancelledTaskMap.forEach { (hashCode, task) ->
            this.startTask(task)
            this.cancelledTaskMap.remove(hashCode)
        }
    }

    fun showTaskSize() {
        println("task size:%s".format(this.taskMap.size))
        println("cancel task size:%s".format(this.cancelledTaskMap.size))
    }
}