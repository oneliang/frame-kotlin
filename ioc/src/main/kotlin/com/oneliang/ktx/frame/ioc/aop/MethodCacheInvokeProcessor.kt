package com.oneliang.ktx.frame.ioc.aop

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.cache.MemoryCache
import com.oneliang.ktx.frame.ioc.Ioc
import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.lang.reflect.Method

open class MethodCacheInvokeProcessor(memoryCacheSize: Int) : DefaultInvokeProcessor() {
    companion object {
        private val logger = LoggerManager.getLogger(MethodCacheInvokeProcessor::class)
    }

    private val memoryCache = MemoryCache(memoryCacheSize)

    @Throws(Throwable::class)
    override fun invoke(instance: Any, method: Method, args: Array<Any?>): Any? {
        val instanceMethod = instance::class.java.getMethod(method.name, *method.parameterTypes)
        if (!instanceMethod.isAnnotationPresent(MethodCache::class.java)) {
            return super.invoke(instance, method, args)
        }
        val methodCacheAnnotation = instanceMethod.getAnnotation(MethodCache::class.java)
        val parameterJson = args.toJson()
        val cacheKey = instance::class.java.name + Constants.Symbol.AT + method.name + Constants.Symbol.AT + parameterJson
        logger.info("need to cache, method:%s, cache key:%s", instanceMethod, cacheKey)
        return this.memoryCache.getOrSave(cacheKey.MD5String(), methodCacheAnnotation.cacheRefreshTime) {
            super.invoke(instance, method, args)
        }
    }
}
