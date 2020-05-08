package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.logging.LoggerManager

open class InterceptorContext : AbstractContext() {

    companion object {
        private val logger = LoggerManager.getLogger(InterceptorContext::class)
        internal val globalInterceptorBeanMap = mutableMapOf<String, GlobalInterceptorBean>()
        internal val interceptorBeanMap = mutableMapOf<String, InterceptorBean>()
        internal val beforeGlobalInterceptorList = mutableListOf<InterceptorInterface>()
        internal val afterGlobalInterceptorList = mutableListOf<InterceptorInterface>()
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
                    val interceptorInstance: InterceptorInterface = this.classLoader.loadClass(globalInterceptorBean.type).newInstance() as InterceptorInterface
                    globalInterceptorBean.interceptorInstance = interceptorInstance
                    globalInterceptorBeanMap[globalInterceptorBean.id] = globalInterceptorBean
                    objectMap[globalInterceptorBean.id] = interceptorInstance
                    val mode = globalInterceptorBean.mode
                    if (mode == GlobalInterceptorBean.INTERCEPTOR_MODE_BEFORE) {
                        beforeGlobalInterceptorList.add(interceptorInstance)
                    } else if (mode == GlobalInterceptorBean.INTERCEPTOR_MODE_AFTER) {
                        afterGlobalInterceptorList.add(interceptorInstance)
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
                    val interceptorInstance = this.classLoader.loadClass(interceptor.type).newInstance() as InterceptorInterface
                    interceptor.interceptorInstance = interceptorInstance
                    interceptorBeanMap[interceptor.id] = interceptor
                    objectMap[interceptor.id] = interceptorInstance
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
        beforeGlobalInterceptorList.clear()
        afterGlobalInterceptorList.clear()
    }

    /**
     * @return the beforeGlobalInterceptorList
     */
    fun getBeforeGlobalInterceptorList(): List<InterceptorInterface> {
        return beforeGlobalInterceptorList
    }

    /**
     * @return the afterGlobalInterceptorList
     */
    fun getAfterGlobalInterceptorList(): List<InterceptorInterface> {
        return afterGlobalInterceptorList
    }
}
