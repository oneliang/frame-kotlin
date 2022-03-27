package com.oneliang.ktx.frame.test.parallel

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.parallel.*
import com.oneliang.ktx.frame.parallel.cache.CacheData
import com.oneliang.ktx.util.common.toIntSafely
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

class SourceProcessor : ParallelSourceProcessor<String> {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val quit = AtomicBoolean(false)
    private var sequence = 0
    override fun initialize(sourceCacheData: CacheData.Data?) {
        if (sourceCacheData != null) {
            sequence = sourceCacheData.data.toIntSafely()
        }
    }

    override suspend fun process(parallelSourceContext: ParallelSourceContext<String>) {
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
            try{
                this.lock.lock()
                condition.await()
                println("1111111111")
            }finally {
                println("2222222222")
                this.lock.unlock()
            }
//            synchronized(this.lock){
//                this.lock.wait()
//            }
        }
        parallelSourceContext.collect(Constants.String.BLANK, ParallelContextAction.FINISHED)
    }

    fun trigger() {
        this.quit.compareAndSet(false, true)
        try{
            this.lock.lock()
            condition.signal()
            println("3333333333")
        }finally {
            println("4444444444")
            this.lock.unlock()
        }
//        synchronized(this.lock){
//            this.lock.notify()
//        }
    }

    override suspend fun savepoint(sourceCacheData: CacheData.Data) {
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
    val parallelJob = ParallelJob<String>("testParallel")
    val sourceProcessor = SourceProcessor()
    parallelJob.addParallelSourceProcessor(sourceProcessor)
    parallelJob.generateFirstParallelJobStep().addParallelTransformProcessor(object : ParallelTransformProcessor<String, String> {
        override suspend fun process(value: String, parallelContext: ParallelContext<String>) {
            if (value.isBlank()) {
                parallelContext.collect(Constants.String.BLANK)
                return
            }
            parallelContext.collect(value)
        }
    }).addParallelSinkProcessor(object : ParallelSinkProcessor<String> {
        private var count = AtomicInteger()
        override fun initialize(sinkCacheData: CacheData.Data?) {
            if (sinkCacheData != null) {
                count.set(sinkCacheData.data.toIntSafely())
            }
        }

        override suspend fun sink(value: String) {
            if (value.isBlank()) {
                return
            }
            count.incrementAndGet()
            println("sink value:$value")
        }

        override suspend fun savepoint(sinkCacheData: CacheData.Data) {
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
        Thread.sleep(10000)
        sourceProcessor.trigger()
    }
    parallelExecutor.execute(parallelJob)
//    parallelExecutor.execute(parallelJob)
}