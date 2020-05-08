package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.parallel.cache.CacheData

interface ParallelSinkProcessor<IN> {

    fun initialize(sinkCacheData: CacheData.Data?) {}

    suspend fun sink(value: IN)

    suspend fun savepoint(sinkCacheData: CacheData.Data) {}

    val cacheKey: String
        get() {
            return Constants.String.BLANK
        }
}