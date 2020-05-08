package com.oneliang.ktx.frame.cache

import kotlin.reflect.KClass

interface CacheManager {

    /**
     * get from cache
     * @param key
     * @param cacheType
     * @return T
     */
    fun <T : Any> getFromCache(key: Any, cacheType: KClass<T>): T?

    /**
     * save to cache
     * @param key
     * @param value
     */
    fun <T : Any> saveToCache(key: Any, value: T)
}