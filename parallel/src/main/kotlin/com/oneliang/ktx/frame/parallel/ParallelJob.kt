package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.cache.CacheManager
import com.oneliang.ktx.frame.cache.FileCacheManager
import com.oneliang.ktx.frame.parallel.cache.*
import com.oneliang.ktx.util.common.findAllChild
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.*
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class ParallelJob<IN>(private val jobName: String, internal val parallelJobConfiguration: ParallelJobConfiguration = ParallelJobConfiguration()) {
    companion object {
        private val logger = LoggerManager.getLogger(ParallelJob::class)
    }

    private val parallelSourceProcessorSet = hashSetOf<ParallelSourceProcessor<IN>>()
    private val firstParallelJobStepList = mutableListOf<ParallelJobStep<IN>>()
    private lateinit var countDownLatch: CountDownLatch
    private lateinit var cacheManager: CacheManager
    private var cacheData: CacheData? = null

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        logger.error("Throws an exception with message: ${throwable.message}", throwable)
    }

    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = EmptyCoroutineContext
    }

    fun addParallelSourceProcessor(parallelSourceProcessor: ParallelSourceProcessor<IN>) {
        this.parallelSourceProcessorSet += parallelSourceProcessor
    }

    fun generateFirstParallelJobStep(): ParallelJobStep<IN> {
        val firstParallelJobStep = ParallelJobStep<IN>()//parallel job step is not include the source processor
        this.firstParallelJobStepList += firstParallelJobStep
        return firstParallelJobStep
    }

    /**
     * only run a thread, not run in a coroutine
     */
    @Suppress("UNCHECKED_CAST")
    internal fun execute() {
        val begin = System.currentTimeMillis()
        if (this.parallelJobConfiguration.useCache) {
            if (this.parallelJobConfiguration.async) {
                "not support async when use cache, async:%s, useCache:%s".format(this.parallelJobConfiguration.async, this.parallelJobConfiguration.useCache).also {
                    logError(it)
                    error(it)
                }
            }
            this.cacheManager = FileCacheManager(this.parallelJobConfiguration.cacheDirectory, cacheRefreshCycle = FileCacheManager.CacheRefreshCycle.NONE)
        }
        logInfo("execute")
        this.countDownLatch = CountDownLatch(this.parallelSourceProcessorSet.size)
        runBlocking(this.coroutineScope) {
            if (this.parallelJobConfiguration.useCache) {
                val json = this.cacheManager.getFromCache(this.jobName, String::class).nullToBlank()
                this.cacheData = CacheData.fromJson(json)
                initializeAllSinkProcessorFromCache((this.firstParallelJobStepList as List<ParallelJobStep<Any>>), this.cacheData)
            }
            val sourceCacheKeySet = hashSetOf<String>()
            for (parallelSourceProcessor in this.parallelSourceProcessorSet) {
                parallelSourceProcessor as ParallelSourceProcessor<Any>
                val sourceCacheKey = parallelSourceProcessor.cacheKey
                if (sourceCacheKeySet.contains(parallelSourceProcessor.cacheKey)) {
                    "duplicate cache key for source processor, source cache key:%s, source processor:%s".format(sourceCacheKey, parallelSourceProcessor).also {
                        logError(it)
                        error(it)
                    }
                } else {
                    sourceCacheKeySet += sourceCacheKey
                }
                val sourceData = this.cacheData?.getSourceData(sourceCacheKey)
                parallelSourceProcessor.initialize(sourceData)
                launch(this.coroutineScope) {
                    val parallelJob = this as ParallelJob<Any>
                    val parallelSourceContext = DefaultParallelSourceContext(this.coroutineScope, parallelSourceProcessor, this.firstParallelJobStepList, parallelJob)
                    parallelSourceProcessor.process(parallelSourceContext)
                }
            }
        }
        this.countDownLatch.await()
        logInfo("before finish, waiting for timeout:%s", this.parallelJobConfiguration.timeoutAfterFinished)
        Thread.sleep(this.parallelJobConfiguration.timeoutAfterFinished)
        logInfo("after finish")
        val cost = System.currentTimeMillis() - begin
        logInfo("execute finished, cost:%s", cost)
    }

    @Suppress("UNCHECKED_CAST")
    private fun initializeAllSinkProcessorFromCache(parallelJobStepList: List<ParallelJobStep<Any>>, cacheData: CacheData?) {
        val list = parallelJobStepList.findAllChild(isChild = {
            it.isParallelSinkProcessor()
        }, hasChild = {
            it.hasNextParallelJobStep()
        }, whenHasChild = {
            it.nextParallelJobStep
        })
        list.forEach { parallelJobStep ->
            if (parallelJobStep.isParallelSinkProcessor()) {
                parallelJobStep.parallelSinkProcessorList.forEach {
                    if (cacheData != null) {
                        val sinkKey = it.cacheKey
                        val sinkData = cacheData.getSinkData(sinkKey)
                        it.initialize(sinkData)
                    }
                }
            } else {
                logError("initialize all sink processor for cache")
            }
        }
    }

    internal fun finish() {
        this.countDownLatch.countDown()
    }

    internal fun getSourceData(sourceKey: String): CacheData.Data? {
        return if (this.parallelJobConfiguration.useCache) {
            this.cacheData?.getSourceData(sourceKey)
        } else null
    }

    internal fun getSinkData(sinkKey: String): CacheData.Data? {
        return if (this.parallelJobConfiguration.useCache) {
            this.cacheData?.getSinkData(sinkKey)
        } else null
    }

    internal fun updateSourceData(key: String, data: CacheData.Data) {
        if (this.parallelJobConfiguration.useCache) {
            this.cacheData?.updateSourceData(key, data)
        }
    }

    internal fun updateSinkData(key: String, sinkData: CacheData.Data) {
        if (this.parallelJobConfiguration.useCache) {
            this.cacheData?.updateSinkData(key, sinkData)
        }
    }

    internal fun saveCache() {
        if (this.parallelJobConfiguration.useCache) {
            logInfo("saving cache.")
            this.cacheManager.saveToCache(jobName, this.cacheData?.toJson().nullToBlank())
        }
    }

    fun clear() {
        this.parallelSourceProcessorSet.clear()
        this.firstParallelJobStepList.clear()
    }

    private fun logError(message: String, vararg args: Any) {
        logger.error(Constants.Symbol.MIDDLE_BRACKET_LEFT + this.jobName + Constants.Symbol.MIDDLE_BRACKET_RIGHT + message, *args)
    }

    private fun logInfo(message: String, vararg args: Any) {
        logger.info(Constants.Symbol.MIDDLE_BRACKET_LEFT + this.jobName + Constants.Symbol.MIDDLE_BRACKET_RIGHT + message, *args)
    }

    private fun runBlocking(coroutineScope: CoroutineScope, block: suspend () -> Unit) {
        runBlocking(coroutineScope.coroutineContext + exceptionHandler) {
            block()
        }
    }

    private fun launch(coroutineScope: CoroutineScope, block: suspend () -> Unit): Job {
        return coroutineScope.launch(coroutineScope.coroutineContext + exceptionHandler) {
            block()
        }
    }
}