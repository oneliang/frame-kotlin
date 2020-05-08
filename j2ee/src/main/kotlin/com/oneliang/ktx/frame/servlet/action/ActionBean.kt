package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.parseRegexGroup

open class ActionBean {

    companion object {
        private const val REGEX = "\\{([\\w]*)\\}"
        const val TAG_ACTION = "action"
    }
    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    var id: String = Constants.String.BLANK
    /**
     * @return the type
     */
    /**
     * @param type the type to set
     */
    var type: String? = null
    /**
     * @return the path
     */
    /**
     * @param path the path to set
     */
    var path: String = Constants.String.BLANK
    /**
     * @return the httpRequestMethods
     */
    /**
     * @param httpRequestMethods the httpRequestMethods to set
     */
    var httpRequestMethods: String = Constants.String.BLANK
        set(httpRequestMethods) {
            field = httpRequestMethods
            if (this.httpRequestMethods.isNotBlank()) {
                this.httpRequestMethodsCode = 0
                val httpRequestMethodArray = this.httpRequestMethods.split(Constants.Symbol.COMMA)
                for (httpRequestMethod in httpRequestMethodArray) {
                    when {
                        httpRequestMethod.equals(Constants.Http.RequestMethod.PUT.value, ignoreCase = true) -> this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.PUT.code
                        httpRequestMethod.equals(Constants.Http.RequestMethod.DELETE.value, ignoreCase = true) -> this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.DELETE.code
                        httpRequestMethod.equals(Constants.Http.RequestMethod.GET.value, ignoreCase = true) -> this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.GET.code
                        httpRequestMethod.equals(Constants.Http.RequestMethod.POST.value, ignoreCase = true) -> this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.POST.code
                        httpRequestMethod.equals(Constants.Http.RequestMethod.HEAD.value, ignoreCase = true) -> this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.HEAD.code
                        httpRequestMethod.equals(Constants.Http.RequestMethod.OPTIONS.value, ignoreCase = true) -> this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.OPTIONS.code
                        httpRequestMethod.equals(Constants.Http.RequestMethod.TRACE.value, ignoreCase = true) -> this.httpRequestMethodsCode = this.httpRequestMethodsCode or ActionInterface.HttpRequestMethod.TRACE.code
                    }
                }
            }
        }
    /**
     * @return the httpRequestMethodsCode
     */
    var httpRequestMethodsCode = ActionInterface.HttpRequestMethod.GET.code or ActionInterface.HttpRequestMethod.POST.code
        private set
    /**
     * @return the actionInstance
     */
    /**
     * @param actionInstance the actionInstance to set
     */
    var actionInstance: Any? = null
    val actionInterceptorBeanList = mutableListOf<ActionInterceptorBean>()
    val beforeActionInterceptorBeanList = mutableListOf<ActionInterceptorBean>()
    val afterActionInterceptorBeanList = mutableListOf<ActionInterceptorBean>()
    val actionForwardBeanList = mutableListOf<ActionForwardBean>()

    /**
     * find forward path
     * @param forward
     * @return forward path
     */
    fun findForwardPath(forward: String): String {
        var forwardPath = Constants.String.BLANK
        for (actionForwardBean in actionForwardBeanList) {
            val forwardName = actionForwardBean.name
            if (forwardName == forward) {
                forwardPath = actionForwardBean.path
                break
            }
        }
        return forwardPath
    }

    /**
     * find action forward bean by static parameter
     * @param parameterMap
     * @return boolean
     */
    fun findActionForwardBeanByStaticParameter(parameterMap: Map<String, Array<String>>): ActionForwardBean? {
        var forwardBean: ActionForwardBean? = null
        for (actionForwardBean in actionForwardBeanList) {
            if (actionForwardBean.isContainsParameters(parameterMap)) {
                forwardBean = actionForwardBean.clone()
                this.replaceActionForwardBeanStaticFilePath(forwardBean, parameterMap)
                break
            }
        }
        return forwardBean
    }

    /**
     * replace action forward bean static file path
     * @param actionForwardBean
     * @param parameterMap
     */
    private fun replaceActionForwardBeanStaticFilePath(actionForwardBean: ActionForwardBean, parameterMap: Map<String, Array<String>>) {
        val staticFilePath = actionForwardBean.staticFilePath
        if (staticFilePath.isNotBlank()) {
            var staticFilePathResult = staticFilePath
            val groupList = staticFilePath.parseRegexGroup(REGEX)
            for (group in groupList) {
                val parameterValues = parameterMap[group]
                if (parameterValues != null && parameterValues.isNotEmpty()) {
                    staticFilePathResult = staticFilePathResult.replaceFirst(REGEX.toRegex(), parameterValues[0])
                }
            }
            actionForwardBean.staticFilePath = staticFilePathResult
        }
    }

    /**
     * addInterceptor
     * @param actionInterceptorBean
     */
    fun addActionBeanInterceptor(actionInterceptorBean: ActionInterceptorBean?) {
        if (actionInterceptorBean != null) {
            val interceptorMode = actionInterceptorBean.mode
            if (interceptorMode == ActionInterceptorBean.Mode.BEFORE) {
                this.beforeActionInterceptorBeanList.add(actionInterceptorBean)
            } else if (interceptorMode == ActionInterceptorBean.Mode.AFTER) {
                this.afterActionInterceptorBeanList.add(actionInterceptorBean)
            }
            this.actionInterceptorBeanList.add(actionInterceptorBean)
        }
    }

    /**
     * add action forward bean
     * @param actionForwardBean
     * @return boolean
     */
    fun addActionForwardBean(actionForwardBean: ActionForwardBean): Boolean {
        return this.actionForwardBeanList.add(actionForwardBean)
    }

    /**
     * is contain http request method
     * @param httpRequestMethod
     * @return boolean
     */
    fun isContainHttpRequestMethod(httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        return httpRequestMethod.code == this.httpRequestMethodsCode and httpRequestMethod.code
    }
}
