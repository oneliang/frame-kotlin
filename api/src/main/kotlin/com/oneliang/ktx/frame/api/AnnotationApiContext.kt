package com.oneliang.ktx.frame.api

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

class AnnotationApiContext : AbstractContext() {
    companion object {
        internal val logger = LoggerManager.getLogger(AnnotationApiContext::class)
        internal val apiClassList = CopyOnWriteArrayList<KClass<*>>()
        internal val apiDocumentObjectMap = ConcurrentHashMap<String, Any>()
    }

    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Api::class)
            apiClassList += kClassList
            for (kClass in apiClassList) {
                logger.info(kClass.toString())
            }
            val apiDocumentObjectMapClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Api.DocumentObjectMap::class)
            for (apiDocumentObjectMapClass in apiDocumentObjectMapClassList) {
                logger.info(apiDocumentObjectMapClass.toString())
                val apiDocumentObjectMapInstance = apiDocumentObjectMapClass.java.newInstance()
                if (apiDocumentObjectMapInstance is ApiDocumentObjectMap) {
                    val instanceObjectMap = apiDocumentObjectMapInstance.generateApiDocumentObjectMap()
                    apiDocumentObjectMap += instanceObjectMap
                }
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    override fun destroy() {
        apiClassList.clear()
        apiDocumentObjectMap.clear()
    }
}