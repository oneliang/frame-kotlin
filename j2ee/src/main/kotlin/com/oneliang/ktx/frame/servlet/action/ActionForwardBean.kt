package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.RequestUtil
import com.oneliang.ktx.util.common.matchesPattern

class ActionForwardBean : Cloneable {
    companion object {
        const val TAG_FORWARD = "forward"
    }

    /**
     * @return the name
     */
    /**
     * @param name the name to set
     */
    var name: String = Constants.String.BLANK
    /**
     * @return the path
     */
    /**
     * @param path the path to set
     */
    var path: String = Constants.String.BLANK
    /**
     * @return the staticParameters
     */
    /**
     * @param staticParameters the staticParameters to set
     */
    var staticParameters: String = Constants.String.BLANK
        set(staticParameters) {
            field = staticParameters
            val parameterMap = RequestUtil.parseParameterString(this.staticParameters)
            this.parameterMap.putAll(parameterMap)
        }
    /**
     * @return the staticFilePath
     */
    /**
     * @param staticFilePath the staticFilePath to set
     */
    var staticFilePath: String = Constants.String.BLANK
    val parameterMap = mutableMapOf<String, Array<String>>()

    /**
     * is contains parameters
     * @param parameterMap
     * @return boolean
     */
    fun isContainsParameters(parameterMap: Map<String, Array<String>>): Boolean {
        var result = true
        if (!this.parameterMap.isEmpty()) {
            run loop@{
                this.parameterMap.forEach { (settingParameterKey, settingParameterValues) ->
                    if (parameterMap.containsKey(settingParameterKey)) {
                        val parameterValues = parameterMap[settingParameterKey]
                        if (parameterValues != null && settingParameterValues.isNotEmpty() && parameterValues.isNotEmpty()) {
                            if (!parameterValues[0].matchesPattern(settingParameterValues[0])) {
                                result = false
                            }
                        }
                    } else {
                        result = false
                    }
                    if (!result) {
                        return@loop
                    }
                }
            }
        } else {
            result = false
        }
        return result
    }

    /**
     * clone action forward bean
     */
    public override fun clone(): ActionForwardBean {
        val actionForwardBean = ActionForwardBean()
        actionForwardBean.name = this.name
        actionForwardBean.path = this.path
        actionForwardBean.staticParameters = this.staticParameters
        actionForwardBean.staticFilePath = this.staticFilePath
        return actionForwardBean
    }
}
