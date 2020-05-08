package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager

class AnnotationIocContext : IocContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationIocContext::class)
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Ioc::class)
            for (kClass in kClassList) {
                logger.debug("found class:$kClass")
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
                //after inject
                val methods = kClass.java.methods
                for (method in methods) {
                    if (method.isAnnotationPresent(Ioc.AfterInject::class.java)) {
                        val iocAfterInjectBean = IocAfterInjectBean()
                        iocAfterInjectBean.method = method.name
                        iocBean.addIocAfterInjectBean(iocAfterInjectBean)
                    }
                }
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
}
