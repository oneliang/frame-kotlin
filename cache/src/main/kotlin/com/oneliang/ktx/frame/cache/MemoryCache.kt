package com.oneliang.ktx.frame.cache

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.getZeroTime
import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.common.toUtilDate
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

class MemoryCache(private val maxSize: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(MemoryCache::class)
    }

    private val oldCacheKeyMap = ConcurrentHashMap<String, String>()
    private val cacheMap = ConcurrentHashMap<String, Any?>()
    private val lock = ReentrantLock()

    fun getOrSave(key: String, cacheRefreshTime: Long, noCacheBlock: () -> Any?): Any? {
        return getOrSave(key, cacheRefreshTime, noCacheBlock, null)
    }

    fun getOrSave(key: String, cacheRefreshTime: Long, noCacheBlock: () -> Any?, deleteOldCacheCallback: ((oldCache: Any?) -> Unit)? = null): Any? {
        val formatTime = if (cacheRefreshTime <= 0) {
            Constants.String.ZERO
        } else {
            val time = System.currentTimeMillis()
            time.getZeroTime(cacheRefreshTime).toUtilDate().toFormatString(Constants.Time.UNION_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
        }
        val cacheKey = key + Constants.Symbol.UNDERLINE + formatTime
        val cacheData = this.cacheMap[cacheKey]
        return if (cacheData == null) {
            val data = noCacheBlock()
            //first try to delete old cache when new cache does not exist
            val oldCacheKey = this.oldCacheKeyMap[key]
            if (oldCacheKey != null) {
                this.oldCacheKeyMap.remove(key)
                val oldCacheData = this.cacheMap.remove(oldCacheKey)
                deleteOldCacheCallback?.invoke(oldCacheData)
            }
            //check cache size before save cache
            if (this.cacheMap.size >= maxSize) {
                try {
                    this.lock.lock()
                    if (this.cacheMap.size >= maxSize) {
                        logger.info("before clear cache, cache size:%s", this.cacheMap.size)
                        this.cacheMap.clear()
                    }
                } finally {
                    this.lock.unlock()
                }
            }
            this.cacheMap[cacheKey] = data
            this.oldCacheKeyMap[key] = cacheKey
            data
        } else {
            cacheData
        }
    }
}