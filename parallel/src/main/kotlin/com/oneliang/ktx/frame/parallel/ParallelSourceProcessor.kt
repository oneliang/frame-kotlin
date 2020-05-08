package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.parallel.cache.CacheData

interface ParallelSourceProcessor<OUT> {

    fun initialize(sourceCacheData: CacheData.Data?) {}

    suspend fun process(parallelSourceContext: ParallelSourceContext<OUT>)

    suspend fun savepoint(sourceCacheData: CacheData.Data) {}

    val cacheKey: String
        get() {
            return Constants.String.BLANK
        }
}