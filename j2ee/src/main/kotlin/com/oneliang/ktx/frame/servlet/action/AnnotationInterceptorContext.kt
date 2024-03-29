package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.reflect.KClass

class AnnotationInterceptorContext : InterceptorContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationInterceptorContext::class)
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        if (fixParameters.isBlank()) {
            logger.warning("parameters is blank, maybe use dsl initialize, please confirm it.")
            return
        }
        try {
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Interceptor::class)
            for (kClass in kClassList) {
                logger.debug("Annotation interceptor class:%s", kClass)
                processClass(kClass)
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * process class to context
     * @param kClass
     */
    fun processClass(kClass: KClass<*>) {
        if (!ObjectUtil.isInheritanceOrInterfaceImplement(kClass.java, InterceptorInterface::class.java)) {
            logger.error("Annotation interceptor class is not InterceptorInterface, class:%s", kClass)
            return
        }
        val interceptorAnnotation = kClass.java.getAnnotation(Interceptor::class.java)
        val interceptorMode = interceptorAnnotation.mode
        val interceptorOrder = interceptorAnnotation.order
        var id = interceptorAnnotation.id
        if (id.isBlank()) {
            id = kClass.java.simpleName
            id = id.substring(0, 1).lowercase() + id.substring(1)
        }
        if (objectMap.containsKey(id)) {
            logger.warning("Annotation interceptor class has been instantiated, class:%s, id:%s", kClass, id)
            return
        }
        val interceptorInstance = kClass.java.newInstance() as InterceptorInterface
        when (interceptorMode) {
            Interceptor.Mode.GLOBAL_ACTION_BEFORE -> {
                val globalBeforeInterceptor = GlobalInterceptorBean()
                globalBeforeInterceptor.id = id
                globalBeforeInterceptor.mode = GlobalInterceptorBean.INTERCEPTOR_MODE_BEFORE
                globalBeforeInterceptor.interceptorInstance = interceptorInstance
                globalBeforeInterceptor.type = kClass.java.name
                globalBeforeInterceptor.order = interceptorOrder
                globalInterceptorBeanMap[globalBeforeInterceptor.id] = globalBeforeInterceptor
                beforeGlobalInterceptorBeanIterable += globalBeforeInterceptor
            }

            Interceptor.Mode.GLOBAL_ACTION_AFTER -> {
                val globalAfterInterceptor = GlobalInterceptorBean()
                globalAfterInterceptor.id = id
                globalAfterInterceptor.mode = GlobalInterceptorBean.INTERCEPTOR_MODE_AFTER
                globalAfterInterceptor.interceptorInstance = interceptorInstance
                globalAfterInterceptor.type = kClass.java.name
                globalAfterInterceptor.order = interceptorOrder
                globalInterceptorBeanMap[globalAfterInterceptor.id] = globalAfterInterceptor
                afterGlobalInterceptorBeanIterable += globalAfterInterceptor
            }

            Interceptor.Mode.SINGLE_ACTION -> {
                val interceptor = InterceptorBean()
                interceptor.id = id
                interceptor.interceptorInstance = interceptorInstance
                interceptor.type = kClass.java.name
                interceptorBeanMap[interceptor.id] = interceptor
            }
        }
        objectMap[id] = ObjectBean(interceptorInstance, ObjectBean.Type.REFERENCE)
    }
}
