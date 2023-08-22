package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class AnnotationIocContext : IocContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationIocContext::class)
        internal val iocAllowExplicitlyInvokeBeanMap = ConcurrentHashMap<String, IocAllowExplicitlyInvokeBean>()
        internal val afterInstantiate: ((iocBean: IocBean) -> Unit) = { iocBean ->
            val methods = iocBean.beanInstance?.javaClass?.methods ?: emptyArray()
            for (method in methods) {
                //after inject
                if (method.isAnnotationPresent(Ioc.AfterInject::class.java)) {
                    val iocAfterInjectAnnotation = method.getAnnotation(Ioc.AfterInject::class.java)
                    val iocAfterInjectBean = IocAfterInjectBean()
                    iocAfterInjectBean.method = method.name
                    iocAfterInjectBean.async = iocAfterInjectAnnotation.async
                    iocBean.addIocAfterInjectBean(iocAfterInjectBean)
                } else if (method.isAnnotationPresent(Ioc.AllowExplicitlyInvoke::class.java)) {
                    val methodAllowExplicitlyInvokeAnnotation = method.getAnnotation(Ioc.AllowExplicitlyInvoke::class.java)
                    val methodId = methodAllowExplicitlyInvokeAnnotation.id
                    if (!iocAllowExplicitlyInvokeBeanMap.containsKey(methodId)) {
                        val iocInvokedBean = IocAllowExplicitlyInvokeBean()
                        iocInvokedBean.id = methodId
                        iocInvokedBean.proxyInstance = iocBean.proxyInstance
                        iocInvokedBean.proxyMethod = iocBean.proxyInstance?.javaClass?.getMethod(method.name, *method.parameterTypes)
                        iocAllowExplicitlyInvokeBeanMap[methodId] = iocInvokedBean
                    } else {
                        logger.error("ioc context initialize error, duplicate ioc invoked bean id:%s", methodId)
                    }
                }
            }
        }
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
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Ioc::class)
            for (kClass in kClassList) {
                logger.debug("Annotation ioc class:%s", kClass)
                processClass(kClass)
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * process class
     * @param kClass
     */
    fun processClass(kClass: KClass<*>) {
        val iocAnnotation = kClass.java.getAnnotation(Ioc::class.java)
        val iocBean = IocBean()
        var id = iocAnnotation.id
        if (id.isBlank()) {
            val classes: Array<Class<*>>? = kClass.java.interfaces
            id = if (!classes.isNullOrEmpty()) {
                classes[0].simpleName
            } else {
                kClass.java.simpleName
            }
            id = id.substring(0, 1).lowercase() + id.substring(1)
        }
        iocBean.id = id
        iocBean.type = kClass.java.name
        iocBean.injectType = iocAnnotation.injectType
        iocBean.proxy = iocAnnotation.proxy
        iocBean.beanClass = kClass.java
        iocBean.afterInstantiate = afterInstantiate
        if (!iocBeanMap.containsKey(iocBean.id)) {
            iocBeanMap[iocBean.id] = iocBean
        } else {
            logger.error("annotation ioc context initialize error, duplicate ioc bean id:%s", iocBean.id)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    fun <T> explicitlyInvoke(methodId: String, vararg args: Any?): T? {
        val iocInvokedBean = iocAllowExplicitlyInvokeBeanMap[methodId]
        return if (iocInvokedBean != null) {
            iocInvokedBean.proxyMethod?.invoke(iocInvokedBean.proxyInstance, *args) as T?
        } else {
            logger.warning("ioc invoked method is not found, method id:%s", methodId)
            null
        }
    }
}
