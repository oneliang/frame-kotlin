package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.concurrent.atomic.AtomicTreeSet
import com.oneliang.ktx.util.logging.LoggerManager

open class InterceptorContext : AbstractContext() {

    companion object {
        private val logger = LoggerManager.getLogger(InterceptorContext::class)
        internal val globalInterceptorBeanMap = mutableMapOf<String, GlobalInterceptorBean>()
        internal val interceptorBeanMap = mutableMapOf<String, InterceptorBean>()
        internal val beforeGlobalInterceptorBeanIterable = AtomicTreeSet<GlobalInterceptorBean> { o1, o2 -> o1.order.compareTo(o2.order) }
        internal val afterGlobalInterceptorBeanIterable = AtomicTreeSet<GlobalInterceptorBean> { o1, o2 -> o1.order.compareTo(o2.order) }
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            //global interceptor list
            val globalInterceptorElementList = root.getElementsByTagName(GlobalInterceptorBean.TAG_GLOBAL_INTERCEPTOR)
            if (globalInterceptorElementList != null) {
                val globalInterceptorLength = globalInterceptorElementList.length
                for (index in 0 until globalInterceptorLength) {
                    val globalInterceptorElement = globalInterceptorElementList.item(index)
                    val globalInterceptorBean = GlobalInterceptorBean()
                    val attributeMap = globalInterceptorElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(globalInterceptorBean, attributeMap)
                    if (objectMap.containsKey(globalInterceptorBean.id)) {
                        logger.warning("Global interceptor class has been instantiated, type:%s, id:%s", globalInterceptorBean.type, globalInterceptorBean.id)
                        continue
                    }
                    val interceptorInstance: InterceptorInterface = this.classLoader.loadClass(globalInterceptorBean.type).newInstance() as InterceptorInterface
                    globalInterceptorBean.interceptorInstance = interceptorInstance
                    globalInterceptorBeanMap[globalInterceptorBean.id] = globalInterceptorBean
                    objectMap[globalInterceptorBean.id] = ObjectBean(interceptorInstance, ObjectBean.Type.REFERENCE)
                    val mode = globalInterceptorBean.mode
                    if (mode == GlobalInterceptorBean.INTERCEPTOR_MODE_BEFORE) {
                        beforeGlobalInterceptorBeanIterable += globalInterceptorBean
                    } else if (mode == GlobalInterceptorBean.INTERCEPTOR_MODE_AFTER) {
                        afterGlobalInterceptorBeanIterable += globalInterceptorBean
                    }
                }
            }
            //interceptor list
            val interceptorElementList = root.getElementsByTagName(InterceptorBean.TAG_INTERCEPTOR)
            if (interceptorElementList != null) {
                val interceptorElementLength = interceptorElementList.length
                for (index in 0 until interceptorElementLength) {
                    val interceptorElement = interceptorElementList.item(index)
                    val interceptor = InterceptorBean()
                    val attributeMap = interceptorElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(interceptor, attributeMap)
                    if (objectMap.containsKey(interceptor.id)) {
                        logger.warning("Interceptor class has been instantiated, type:%s, id:%s", interceptor.type, interceptor.id)
                        continue
                    }
                    val interceptorInstance = this.classLoader.loadClass(interceptor.type).newInstance() as InterceptorInterface
                    interceptor.interceptorInstance = interceptorInstance
                    interceptorBeanMap[interceptor.id] = interceptor
                    objectMap[interceptor.id] = ObjectBean(interceptorInstance, ObjectBean.Type.REFERENCE)
                }
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
        globalInterceptorBeanMap.clear()
        interceptorBeanMap.clear()
        beforeGlobalInterceptorBeanIterable.clear()
        afterGlobalInterceptorBeanIterable.clear()
    }

    /**
     * @return the beforeGlobalInterceptorBeanIterable
     */
    fun getBeforeGlobalInterceptorBeanIterable(): Iterable<GlobalInterceptorBean> {
        return beforeGlobalInterceptorBeanIterable
    }

    /**
     * @return the afterGlobalInterceptorBeanIterable
     */
    fun getAfterGlobalInterceptorBeanIterable(): Iterable<GlobalInterceptorBean> {
        return afterGlobalInterceptorBeanIterable
    }
}
