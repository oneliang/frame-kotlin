package com.oneliang.ktx.frame.test.parallel

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.parallel.*
import com.oneliang.ktx.frame.parallel.cache.CacheData
import com.oneliang.ktx.util.common.toIntSafely
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    val parallelExecutor = ParallelJobExecutor()
    val parallelJob = ParallelJob<String>("testParallel", ParallelJobConfiguration().apply {
        this.async = false
        this.useCache = true
        this.cacheDirectory = "/D:/cache"
    })
    parallelJob.addParallelSourceProcessor(object : ParallelSourceProcessor<String> {
        private var sequence = 0
        override fun initialize(sourceCacheData: CacheData.Data?) {
            if (sourceCacheData != null) {
                sequence = sourceCacheData.data.toIntSafely()
            }
        }

        override suspend fun process(parallelSourceContext: ParallelSourceContext<String>) {
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
                        parallelSourceContext.collect("3", ParallelContextAction.FINISHED)
                    }
                }
                Thread.sleep(1000)
            }
            parallelSourceContext.collect(Constants.String.BLANK, ParallelContextAction.FINISHED)
        }

        override suspend fun savepoint(sourceCacheData: CacheData.Data) {
            sourceCacheData.data = sequence.toString()
        }

        override val cacheKey: String
            get() = "sequence"
    })
    parallelJob.generateFirstParallelJobStep().addParallelTransformProcessor(object : ParallelTransformProcessor<String, String> {
        override suspend fun process(value: String, parallelContext: ParallelContext<String>) {
            if (value.isBlank()) {
                parallelContext.collect(Constants.String.BLANK)
                return
            }
            parallelContext.collect("a")
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
//            parallelContext.collect("b")
//        }
//    }).addParallelSinkProcessor(object : ParallelSinkProcessor<String> {
//        override suspend fun sink(value: String) {
//            println("sink value:$value")
//        }
//    })
    parallelExecutor.execute(parallelJob)
//    parallelExecutor.execute(parallelJob)
}