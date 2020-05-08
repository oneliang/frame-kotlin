package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap

open class ActionContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(ActionContext::class)
        internal val actionBeanMap: MutableMap<String, ActionBean> = ConcurrentHashMap()
        internal val pathActionBeanMap: MutableMap<String, MutableList<ActionBean>> = ConcurrentHashMap()
        internal val globalForwardBeanMap: MutableMap<String, GlobalForwardBean> = ConcurrentHashMap()
        internal val globalForwardMap: MutableMap<String, String> = ConcurrentHashMap()
        internal val globalExceptionForwardBean = GlobalExceptionForwardBean()
    }

    /**
     * @return the globalExceptionForwardBean
     */
    val globalExceptionForwardPath: String
        get() {
            return globalExceptionForwardBean.path
        }

    /**
     * initialize
     * @param parameters
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            //global forward list
            val globalForwardElementList = root.getElementsByTagName(GlobalForwardBean.TAG_GLOBAL_FORWARD)
            if (globalForwardElementList != null) {
                val length = globalForwardElementList.length
                for (index in 0 until length) {
                    val globalForwardBean = GlobalForwardBean()
                    val globalForward = globalForwardElementList.item(index)
                    val attributeMap = globalForward.attributes
                    JavaXmlUtil.initializeFromAttributeMap(globalForwardBean, attributeMap)
                    val globalForwardBeanName = globalForwardBean.name
                    globalForwardBeanMap[globalForwardBeanName] = globalForwardBean
                    globalForwardMap[globalForwardBeanName] = globalForwardBean.path
                }
            }
            //global exception forward
            val globalExceptionForwardElementList = root.getElementsByTagName(GlobalExceptionForwardBean.TAG_GLOBAL_EXCEPTION_FORWARD)
            if (globalExceptionForwardElementList != null && globalExceptionForwardElementList.length > 0) {
                val attributeMap = globalExceptionForwardElementList.item(0).attributes
                JavaXmlUtil.initializeFromAttributeMap(globalExceptionForwardBean, attributeMap)
            }
            //action list
            val actionElementList = root.getElementsByTagName(ActionBean.TAG_ACTION)
            //xml to object
            if (actionElementList != null) {
                val length = actionElementList.length
                for (index in 0 until length) {
                    val actionElement = actionElementList.item(index)
                    //action bean
                    val actionBean = ActionBean()
                    val attributeMap = actionElement.attributes
                    JavaXmlUtil.initializeFromAttributeMap(actionBean, attributeMap)
                    //node list
                    val childNodeElementList = actionElement.childNodes
                    if (childNodeElementList != null) {
                        val childNodeLength = childNodeElementList.length
                        for (nodeIndex in 0 until childNodeLength) {
                            val childNodeElement = childNodeElementList.item(nodeIndex)
                            val childNodeElementName = childNodeElement.nodeName
                            //interceptorList
                            if (childNodeElementName == ActionInterceptorBean.TAG_INTERCEPTOR) {
                                val actionInterceptorBean = ActionInterceptorBean()
                                val interceptorAttributeMap = childNodeElement.attributes
                                JavaXmlUtil.initializeFromAttributeMap(actionInterceptorBean, interceptorAttributeMap)
                                actionBean.addActionBeanInterceptor(actionInterceptorBean)
                            } else if (childNodeElementName == ActionForwardBean.TAG_FORWARD) {
                                val actionForwardBean = ActionForwardBean()
                                val forwardAttributeMap = childNodeElement.attributes
                                JavaXmlUtil.initializeFromAttributeMap(actionForwardBean, forwardAttributeMap)
                                actionBean.addActionForwardBean(actionForwardBean)
                            }//forwardList
                        }
                    }
                    val actionInstance = objectMap.getOrPut(actionBean.id) { this.classLoader.loadClass(actionBean.type).newInstance() as ActionInterface }
                    actionBean.actionInstance = actionInstance
                    actionBeanMap[actionBean.id] = actionBean
                    val actionBeanList = pathActionBeanMap.getOrPut(actionBean.path) { mutableListOf() }
                    actionBeanList.add(actionBean)
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
        actionBeanMap.clear()
        pathActionBeanMap.clear()
        globalForwardBeanMap.clear()
        globalForwardMap.clear()
    }

    /**
     * interceptor inject
     */
    fun interceptorInject() {
        actionBeanMap.forEach { (_, actionBean) ->
            val actionInterceptorBeanList = actionBean.actionInterceptorBeanList
            for (actionInterceptorBean in actionInterceptorBeanList) {
                if (objectMap.containsKey(actionInterceptorBean.id)) {
                    val interceptorInstance = objectMap[actionInterceptorBean.id] as InterceptorInterface
                    actionInterceptorBean.interceptorInstance = interceptorInstance
                }
            }
        }
    }

    /**
     * find action bean
     * @param path
     * @return List<ActionBean>
    </ActionBean> */
    fun findActionBeanList(path: String): List<ActionBean>? {
        return pathActionBeanMap[path]
    }

    /**
     * find action
     * @param beanId
     * @return action object
     */
    fun findAction(beanId: String): ActionInterface? {
        return objectMap[beanId] as ActionInterface?
    }

    /**
     * find global forward
     * @param name forward name
     * @return forward path
     */
    fun findGlobalForwardPath(name: String): String? {
        return globalForwardMap[name]
    }
}
