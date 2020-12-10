package com.oneliang.ktx.frame.ioc.aop

import com.oneliang.ktx.frame.cache.MemoryCache
import com.oneliang.ktx.frame.ioc.Ioc
import com.oneliang.ktx.util.common.MD5String
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import java.lang.reflect.Method

open class MethodCacheInvokeProcessor(memoryCacheSize: Int) : DefaultInvokeProcessor() {
    companion object {
        private val logger = LoggerManager.getLogger(MethodCacheInvokeProcessor::class)
        //        private val fileCacheManager: FileCacheManager = FileCacheManager("methodCache")
    }

    private val memoryCache = MemoryCache(memoryCacheSize)

    @Throws(Throwable::class)
    override fun invoke(instance: Any, method: Method, args: Array<Any>): Any? {
        val instanceMethod = instance::class.java.getMethod(method.name, *method.parameterTypes)
        if (!instanceMethod.isAnnotationPresent(MethodCache::class.java)) {
            return super.invoke(instance, method, args)
        }
        val methodCacheAnnotation = instanceMethod.getAnnotation(MethodCache::class.java)
        val parameterJson = args.toJson()
        logger.info("need to cache, method:%s, parameter json:%s", instanceMethod, parameterJson)
        return this.memoryCache.getOrSave(parameterJson.MD5String(), methodCacheAnnotation.cacheRefreshTime) {
            super.invoke(instance, method, args)
        }
    }

//    private fun a(){
//        val cacheDataKey = parameterJson.MD5String()
//        logger.info("parameter json:%s, cache data key:%s", parameterJson, cacheDataKey)
//        val cacheData = fileCacheManager.getFromCache(cacheDataKey, String::class)
//        return if (cacheData == null || cacheData.isBlank()) {
//            val data = super.invoke(instance, method, args)
//            fileCacheManager.saveToCache(cacheDataKey, data?.toJson() ?: Constants.String)
//            data
//        } else {
//            val methodCacheAnnotation = instanceMethod.getAnnotation(MethodCache::class.java)
//            val listFieldMap = methodCacheAnnotation.listFields.toMap { it.field to it }
//            cacheData.jsonToObject(method.returnType.kotlin, object : DefaultKotlinClassProcessor() {
//                override fun <T : Any> changeClassProcess(kClass: KClass<T>, values: Array<String>, fieldName: String): Any? {
//                    val listField = listFieldMap[fieldName]
//                    if (listField != null && values[0].isNotEmpty()) {
//                        return values[0].jsonToObject(listField.kClass)
//                    } else {
//                        return super.changeClassProcess(kClass, values, fieldName)
//                    }
//                }
//            })
//        }
//    }
}
