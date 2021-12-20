package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.ActionBean
import com.oneliang.ktx.frame.servlet.action.ActionContext
import com.oneliang.ktx.frame.servlet.action.GlobalInterceptorBean
import com.oneliang.ktx.frame.servlet.action.InterceptorContext
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.file.saveTo
import com.oneliang.ktx.util.file.toPropertiesAutoCreate
import java.io.File
import java.util.*

/**
 * before global interceptor iterable
 */
val ConfigurationContext.beforeGlobalInterceptorBeanIterable: Iterable<GlobalInterceptorBean>
    get() {
        var beforeGlobalInterceptorBeanIterable: Iterable<GlobalInterceptorBean> = emptyList()
        val interceptorContext = this.findContext(InterceptorContext::class)
        if (interceptorContext != null) {
            beforeGlobalInterceptorBeanIterable = interceptorContext.getBeforeGlobalInterceptorBeanIterable()
        }
        return beforeGlobalInterceptorBeanIterable
    }

/**
 * after global interceptor iterable
 */
val ConfigurationContext.afterGlobalInterceptorBeanIterable: Iterable<GlobalInterceptorBean>
    get() {
        var afterGlobalInterceptorBeanIterable: Iterable<GlobalInterceptorBean> = emptyList()
        val interceptorContext = this.findContext(InterceptorContext::class)
        if (interceptorContext != null) {
            afterGlobalInterceptorBeanIterable = interceptorContext.getAfterGlobalInterceptorBeanIterable()
        }
        return afterGlobalInterceptorBeanIterable
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

fun ConfigurationContext.outputActionLevel(outputFilename: String) {
    if (outputFilename.isBlank()) {
        return
    }
    val levelActionBeanListMap = mutableMapOf<String, TreeMap<String, ActionBean>>()
    ActionContext.actionBeanMap.forEach { (_, actionBean) ->
        val actionBeanTreeMap = levelActionBeanListMap.getOrPut(actionBean.level) { TreeMap() }
        actionBeanTreeMap[actionBean.path] = actionBean
    }
    val dotLastIndex = outputFilename.lastIndexOf(Constants.Symbol.DOT)
    var prefix = outputFilename
    var suffix = Constants.String.BLANK
    if (dotLastIndex >= 0) {
        prefix = outputFilename.substring(0, dotLastIndex)
        suffix = outputFilename.substring(dotLastIndex)
    }
    val levelValues = ActionBean.Level.values()
    levelValues.forEach {
        val actionBeanTreeMap = levelActionBeanListMap[it.value]
        if (!actionBeanTreeMap.isNullOrEmpty()) {
            val filename = prefix + Constants.Symbol.UNDERLINE + it.value + suffix
            val file = File(this.projectRealPath, filename)
            val properties = file.toPropertiesAutoCreate()
            actionBeanTreeMap.forEach { (_, actionBean) ->
                properties.setProperty(actionBean.path, actionBean.path)
            }
            properties.saveTo(file)
        }
    }
}