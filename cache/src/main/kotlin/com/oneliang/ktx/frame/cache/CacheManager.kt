package com.oneliang.ktx.frame.cache

import kotlin.reflect.KClass

interface CacheManager {

    /**
     * get from cache
     * @param key
     * @param cacheType
     * @param cacheRefreshTime, default -1
     * @return T
     */
    fun <T : Any> getFromCache(key: Any, cacheType: KClass<T>, cacheRefreshTime: Long = -1L): T?

    /**
     * save to cache
     * @param key
     * @param value
     * @param cacheRefreshTime, default -1
     */
    fun <T : Any> saveToCache(key: Any, value: T, cacheRefreshTime: Long = -1L)
}