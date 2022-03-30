package com.oneliang.ktx.frame.websocket

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager

class AnnotationWebSocketInterceptorContext : AbstractContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationWebSocketInterceptorContext::class)
        internal val globalWebSocketInterceptorBeanMap = mutableMapOf<String, GlobalWebSocketInterceptorBean>()
        internal val beforeGlobalWebSocketInterceptorList = mutableListOf<WebSocketInterceptorInterface>()
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, WebSocketInterceptor::class)
            for (kClass in kClassList) {
                logger.debug("Annotation web socket interceptor class:%s", kClass)
                if (!ObjectUtil.isInheritanceOrInterfaceImplement(kClass.java, WebSocketInterceptorInterface::class.java)) {
                    logger.error("Annotation web socket interceptor class is not WebSocketInterceptorInterface, class:%s", kClass)
                    continue
                }
                val interceptorAnnotation = kClass.java.getAnnotation(WebSocketInterceptor::class.java)
                val interceptorMode = interceptorAnnotation.mode
                var id = interceptorAnnotation.id
                if (id.isBlank()) {
                    id = kClass.java.simpleName
                    id = id.substring(0, 1).toLowerCase() + id.substring(1)
                }
                if (objectMap.containsKey(id)) {
                    logger.warning("Annotation interceptor class has been instantiated, class:%s, id:%s", kClass, id)
                    continue
                }
                val webSocketInterceptorInstance = kClass.java.newInstance() as WebSocketInterceptorInterface
                when (interceptorMode) {
                    WebSocketInterceptor.Mode.GLOBAL_ACTION_BEFORE -> {
                        val globalBeforeWebSocketInterceptor = GlobalWebSocketInterceptorBean()
                        globalBeforeWebSocketInterceptor.id = id
                        globalBeforeWebSocketInterceptor.mode = GlobalWebSocketInterceptorBean.INTERCEPTOR_MODE_BEFORE
                        globalBeforeWebSocketInterceptor.interceptorInstance = webSocketInterceptorInstance
                        globalBeforeWebSocketInterceptor.type = kClass.java.name
                        globalWebSocketInterceptorBeanMap[globalBeforeWebSocketInterceptor.id] = globalBeforeWebSocketInterceptor
                        beforeGlobalWebSocketInterceptorList.add(webSocketInterceptorInstance)
                    }
                }
                objectMap[id] = ObjectBean(webSocketInterceptorInstance, ObjectBean.Level.REFERENCE)
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * destroy
     */
    override fun destroy() {
        globalWebSocketInterceptorBeanMap.clear()
        beforeGlobalWebSocketInterceptorList.clear()
    }

    /**
     * @return the beforeGlobalWebSocketInterceptorList
     */
    fun getBeforeGlobalWebSocketInterceptorList(): List<WebSocketInterceptorInterface> {
        return beforeGlobalWebSocketInterceptorList
    }
}
