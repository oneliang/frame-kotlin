package com.oneliang.ktx.frame.test.parallel

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.parallel.*
import com.oneliang.ktx.frame.parallel.cache.CacheData
import com.oneliang.ktx.frame.parallel.processor.QueueParallelSourceProcessor
import com.oneliang.ktx.util.common.toIntSafely
import com.oneliang.ktx.util.concurrent.atomic.AwaitAndSignal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SourceProcessor : ParallelSourceProcessor<String> {
    companion object {
        private const val NEED_TO_FETCH = "NEED_TO_FETCH"
    }

    private val awaitAndSignal = AwaitAndSignal<String>()
    private val quit = AtomicBoolean(false)
    private var sequence = 0

    override fun initialize(sourceCacheData: CacheData.Data?) {
        if (sourceCacheData != null) {
            sequence = sourceCacheData.data.toIntSafely()
        }
    }

    override fun process(parallelSourceContext: ParallelSourceContext<String>) {
        while (!quit.get()) {
            for (i in 1..3) {
                if (i <= sequence) {
                    continue
                }
                sequence = i
                when (i) {
                    1 -> {
                        parallelSourceContext.collect("1", ParallelContextAction.NONE)
                    }

                    2 -> {
                        parallelSourceContext.collect("2", ParallelContextAction.SAVEPOINT)
                    }

                    3 -> {
                        parallelSourceContext.collect("3", ParallelContextAction.NONE)
                    }
                }
                Thread.sleep(1000)
            }
            this.awaitAndSignal.await(NEED_TO_FETCH, {
                println(Thread.currentThread().id.toString() + ":0000000000")
            }, {
                println(Thread.currentThread().id.toString() + ":1111111111")
            })
//            synchronized(this.lock){
//                this.lock.wait()
//            }
        }
        parallelSourceContext.collect(Constants.String.BLANK, ParallelContextAction.FINISHED)
    }

    fun trigger() {
        this.quit.compareAndSet(false, true)

        this.awaitAndSignal.signal(NEED_TO_FETCH, true, {
            println(Thread.currentThread().id.toString() + ":3333333333")
        }, {
            println(Thread.currentThread().id.toString() + ":4444444444")
        })
//        synchronized(this.lock){
//            this.lock.notify()
//        }
    }

    override fun savepoint(sourceCacheData: CacheData.Data) {
        sourceCacheData.data = sequence.toString()
    }

    override val cacheKey: String
        get() = "sequence"
}

fun main() {
    val parallelExecutor = ParallelJobExecutor()
//    val parallelJob = ParallelJob<String>("testParallel", ParallelJobConfiguration().apply {
//        this.async = false
//        this.useCache = true
//        this.cacheDirectory = "/D:/cache"
//    })
    val parallelJob = ParallelJob<String>("testParallel", ParallelJobConfiguration().also { it.async = true;it.useCache = false })
//    val sourceProcessor = SourceProcessor()
//    parallelJob.addParallelSourceProcessor(sourceProcessor)
    val queueParallelSourceProcessor = QueueParallelSourceProcessor<String>()
    parallelJob.addParallelSourceProcessor(queueParallelSourceProcessor)
    parallelJob.generateFirstParallelJobStep().addParallelTransformProcessor(object : ParallelTransformProcessor<String, String> {
        override fun process(value: String, parallelTransformContext: ParallelTransformContext<String>) {
            if (value.isBlank()) {
                parallelTransformContext.collect(Constants.String.BLANK)
                return
            }
            parallelTransformContext.collect(value)
        }
    }).addParallelTransformProcessor(object : ParallelTransformProcessor<String, String> {
        override fun process(value: String, parallelTransformContext: ParallelTransformContext<String>) {
            if (value.isBlank()) {
                parallelTransformContext.collect(Constants.String.BLANK)
                return
            }
            parallelTransformContext.collect("$value--1")
        }
    }).addParallelTransformProcessor(object : ParallelTransformProcessor<String, String> {
        override fun process(value: String, parallelTransformContext: ParallelTransformContext<String>) {
            if (value.isBlank()) {
                parallelTransformContext.collect(Constants.String.BLANK)
                return
            }
            Thread.sleep(100)
            parallelTransformContext.collect("$value--2")
        }
    }).addParallelSinkProcessor(object : ParallelSinkProcessor<String> {
        private var count = AtomicInteger()
        override fun initialize(sinkCacheData: CacheData.Data?) {
            if (sinkCacheData != null) {
                count.set(sinkCacheData.data.toIntSafely())
            }
        }

        override fun sink(value: String) {
            if (value.isBlank()) {
                return
            }
            count.incrementAndGet()
            println("sink value:$value")
        }

        override fun savepoint(sinkCacheData: CacheData.Data) {
            sinkCacheData.apply {
                this.data = count.get().toString()
            }
        }

        override val cacheKey: String
            get() = "sink"
    })
//    parallelJob.generateFirstParallelJobStep().addParallelTransformProcessor(object : ParallelTransformProcessor<String, String> {
//        override suspend fun process(value: String, parallelContext: ParallelContext<String>) {
//            parallelContext.collect(value)
//        }
//    }).addParallelSinkProcessor(object : ParallelSinkProcessor<String> {
//        override suspend fun sink(value: String) {
//            println("sink value:$value")
//        }
//    })
    GlobalScope.launch {
        Thread.sleep(1000)
//        sourceProcessor.trigger()
        repeat(10) {
            queueParallelSourceProcessor.addResource(it.toString())
        }
    }
    parallelExecutor.execute(parallelJob)
//    parallelExecutor.execute(parallelJob)
}