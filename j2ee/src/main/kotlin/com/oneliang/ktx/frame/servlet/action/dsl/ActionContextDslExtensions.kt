package com.oneliang.ktx.frame.servlet.action.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.ActionBean
import com.oneliang.ktx.frame.servlet.action.ActionContext


class ActionContextDslBean {
    var parameters: String = Constants.String.BLANK
}

fun ConfigurationContext.actionContext(
    id: String = Constants.String.BLANK,
    block: ActionContext.(actionContextDslBean: ActionContextDslBean) -> Unit
) {
    val actionContext = ActionContext()
    val actionContextDslBean = ActionContextDslBean()
    block(actionContext, actionContextDslBean)
    val fixId = id.ifBlank { ActionContext::class.java.name }
    this.addContext(fixId, actionContextDslBean.parameters, actionContext)
}

fun ActionContext.action(block: ActionBean.() -> Unit) {

}


