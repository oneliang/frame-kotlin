package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.common.JavaXmlUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

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
        if (fixParameters.isBlank()) {
            logger.warning("parameters is blank, maybe use dsl initialize, please confirm it.")
            return
        }
        try {
            val path = this.classesRealPath + fixParameters
            val document = JavaXmlUtil.parse(path)
            val root = document.documentElement
            //global forward list
            val globalForwardElementList = root.getElementsByTagName(GlobalForwardBean.TAG_GLOBAL_FORWARD)
            val globalForwardElementListlength = globalForwardElementList.length
            for (index in 0 until globalForwardElementListlength) {
                val globalForwardBean = GlobalForwardBean()
                val globalForward = globalForwardElementList.item(index)
                val attributeMap = globalForward.attributes
                JavaXmlUtil.initializeFromAttributeMap(globalForwardBean, attributeMap)
                val globalForwardBeanName = globalForwardBean.name
                globalForwardBeanMap[globalForwardBeanName] = globalForwardBean
                globalForwardMap[globalForwardBeanName] = globalForwardBean.path
            }
            //global exception forward
            val globalExceptionForwardElementList = root.getElementsByTagName(GlobalExceptionForwardBean.TAG_GLOBAL_EXCEPTION_FORWARD)
            if (globalExceptionForwardElementList.length > 0) {
                val attributeMap = globalExceptionForwardElementList.item(0).attributes
                JavaXmlUtil.initializeFromAttributeMap(globalExceptionForwardBean, attributeMap)
            }
            //action list
            val actionElementList = root.getElementsByTagName(ActionBean.TAG_ACTION) ?: return
            //xml to object
            val actionElementListLength = actionElementList.length
            for (index in 0 until actionElementListLength) {
                val actionElement = actionElementList.item(index)
                //action bean
                val actionBean = ActionBean()
                val attributeMap = actionElement.attributes
                JavaXmlUtil.initializeFromAttributeMap(actionBean, attributeMap)
                //node list
                val childNodeElementList = actionElement.childNodes ?: continue
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
                val actionObjectBean = objectMap.getOrPut(actionBean.id) {
                    ObjectBean(this.classLoader.loadClass(actionBean.type).newInstance() as ActionInterface, ObjectBean.Type.REFERENCE)
                }
                actionBean.actionObjectBean = actionObjectBean
                actionBeanMap[actionBean.id] = actionBean
                val actionBeanList = pathActionBeanMap.getOrPut(actionBean.path) { mutableListOf() }
                actionBeanList.add(actionBean)
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
     * register action
     * @param path
     * @param httpRequestMethods
     * @param actionInterface
     */
    fun registerAction(path: String, httpRequestMethods: Array<Constants.Http.RequestMethod> = arrayOf(Constants.Http.RequestMethod.GET, Constants.Http.RequestMethod.POST), actionInterface: ActionInterface) {
        val actionBean = ActionBean()
        actionBean.path = path
        actionBean.level = ActionBean.Level.PUBLIC.value
        val fixHttpRequestMethods = httpRequestMethods.ifEmpty { arrayOf(Constants.Http.RequestMethod.GET, Constants.Http.RequestMethod.POST) }
        actionBean.httpRequestMethods = fixHttpRequestMethods.joinToString(separator = Constants.Symbol.COMMA)
        val actionObjectBean = objectMap.getOrPut(actionBean.id) {
            ObjectBean(actionInterface, ObjectBean.Type.REFERENCE)
        }
        actionBean.actionObjectBean = actionObjectBean
        actionBeanMap[actionBean.id] = actionBean
        val actionBeanList = pathActionBeanMap.getOrPut(actionBean.path) { mutableListOf() }
        actionBeanList.add(actionBean)
    }

    /**
     * register action
     * @param path
     * @param httpRequestMethods
     * @param executor
     */
    fun registerAction(path: String, httpRequestMethods: Array<Constants.Http.RequestMethod> = arrayOf(Constants.Http.RequestMethod.GET, Constants.Http.RequestMethod.POST), executor: (servletRequest: ServletRequest, servletResponse: ServletResponse) -> String) {
        registerAction(path, httpRequestMethods, object : ActionInterface {
            override fun execute(servletRequest: ServletRequest, servletResponse: ServletResponse): String {
                return executor.invoke(servletRequest, servletResponse)
            }
        })
    }

    /**
     * interceptor inject
     */
    fun interceptorInject() {
        actionBeanMap.forEach { (_, actionBean) ->
            val actionInterceptorBeanList = actionBean.actionInterceptorBeanList
            for (actionInterceptorBean in actionInterceptorBeanList) {
                val objectBean = objectMap[actionInterceptorBean.id]
                if (objectBean != null) {
                    val interceptorInstance = objectBean.instance as InterceptorInterface
                    actionInterceptorBean.interceptorInstance = interceptorInstance
                } else {
                    throw InitializeException("class:%s, action interceptor id not found in object map, interceptor id:%s".format(actionBean.type, actionInterceptorBean.id))
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
     * @param id
     * @return action object
     */
    fun findAction(id: String): ActionInterface? {
        return objectMap[id]?.instance as ActionInterface?
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
