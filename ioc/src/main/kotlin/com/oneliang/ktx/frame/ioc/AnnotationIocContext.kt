package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap

class AnnotationIocContext : IocContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationIocContext::class)
        internal val iocAllowExplicitInvokeBeanMap = ConcurrentHashMap<String, IocAllowExplicitInvokeBean>()
        internal val afterInstantiate: ((iocBean: IocBean) -> Unit) = { iocBean ->
            val methods = iocBean.beanInstance?.javaClass?.methods ?: emptyArray()
            for (method in methods) {
                //after inject
                if (method.isAnnotationPresent(Ioc.AfterInject::class.java)) {
                    val iocAfterInjectBean = IocAfterInjectBean()
                    iocAfterInjectBean.method = method.name
                    iocBean.addIocAfterInjectBean(iocAfterInjectBean)
                } else if (method.isAnnotationPresent(Ioc.AllowExplicitInvoke::class.java)) {
                    val methodInvokeAnnotation = method.getAnnotation(Ioc.AllowExplicitInvoke::class.java)
                    val methodId = methodInvokeAnnotation.id
                    if (!iocAllowExplicitInvokeBeanMap.containsKey(methodId)) {
                        val iocInvokedBean = IocAllowExplicitInvokeBean()
                        iocInvokedBean.id = methodId
                        iocInvokedBean.proxyInstance = iocBean.proxyInstance
                        iocInvokedBean.proxyMethod = iocBean.proxyInstance?.javaClass?.getMethod(method.name, *method.parameterTypes)
                        iocAllowExplicitInvokeBeanMap[methodId] = iocInvokedBean
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
        try {
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Ioc::class)
            for (kClass in kClassList) {
                logger.debug("Annotation ioc class:%s", kClass)
                val iocAnnotation = kClass.java.getAnnotation(Ioc::class.java)
                val iocBean = IocBean()
                var id = iocAnnotation.id
                if (id.isBlank()) {
                    val classes = kClass.java.interfaces
                    id = if (classes != null && classes.isNotEmpty()) {
                        classes[0].simpleName
                    } else {
                        kClass.java.simpleName
                    }
                    id = id.substring(0, 1).toLowerCase() + id.substring(1)
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
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    fun <T> explicitInvoke(methodId: String, vararg args: Any?): T? {
        val iocInvokedBean = iocAllowExplicitInvokeBeanMap[methodId]
        return if (iocInvokedBean != null) {
            iocInvokedBean.proxyMethod?.invoke(iocInvokedBean.proxyInstance, *args) as T?
        } else {
            logger.warning("ioc invoked method is not found, method id:%s", methodId)
            null
        }
    }
}
