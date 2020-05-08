package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.ActionBean
import com.oneliang.ktx.frame.servlet.action.ActionContext
import com.oneliang.ktx.frame.servlet.action.InterceptorContext
import com.oneliang.ktx.frame.servlet.action.InterceptorInterface
import com.oneliang.ktx.util.common.nullToBlank

/**
 * before global interceptor list
 */
val ConfigurationContext.beforeGlobalInterceptorList: List<InterceptorInterface>
    get() {
        var beforeGlobalInterceptorList: List<InterceptorInterface> = emptyList()
        val interceptorContext = this.findContext(InterceptorContext::class)
        if (interceptorContext != null) {
            beforeGlobalInterceptorList = interceptorContext.getBeforeGlobalInterceptorList()
        }
        return beforeGlobalInterceptorList
    }

/**
 * after global interceptor list
 */
val ConfigurationContext.afterGlobalInterceptorList: List<InterceptorInterface>
    get() {
        var afterGlobalInterceptorList: List<InterceptorInterface> = emptyList()
        val interceptorContext = this.findContext(InterceptorContext::class)
        if (interceptorContext != null) {
            afterGlobalInterceptorList = interceptorContext.getAfterGlobalInterceptorList()
        }
        return afterGlobalInterceptorList
    }

/**
 * global exception forward path
 */
val ConfigurationContext.globalExceptionForwardPath: String?
    get() {
        val actionContext = this.findContext(ActionContext::class)
        return actionContext?.globalExceptionForwardPath.nullToBlank()
    }

/**
 * interceptor inject
 */
fun ConfigurationContext.interceptorInject() {
    val actionContext = this.findContext(ActionContext::class)
    actionContext?.interceptorInject()
}

/**
 * find global forward path with name
 *
 * @param name
 * @return String
 */
fun ConfigurationContext.findGlobalForwardPath(name: String): String {
    val actionContext = ConfigurationContainer.rootConfigurationContext.findContext(ActionContext::class)
    return actionContext?.findGlobalForwardPath(name).nullToBlank()
}


/**
 * find ActionBean list
 *
 * @param uri
 * @return List<ActionBean>
</ActionBean> */
fun ConfigurationContext.findActionBeanList(uri: String): List<ActionBean>? {
    val actionContext = ConfigurationContainer.rootConfigurationContext.findContext(ActionContext::class)
    return actionContext?.findActionBeanList(uri)
}