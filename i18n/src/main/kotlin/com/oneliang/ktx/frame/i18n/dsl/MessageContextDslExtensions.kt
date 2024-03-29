package com.oneliang.ktx.frame.i18n.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.i18n.MessageContext


class MessageContextDslBean {
    var parameters: String = Constants.String.BLANK
}

fun ConfigurationContext.messageContext(
    id: String = Constants.String.BLANK,
    block: MessageContext.(MessageContextDslBean) -> Unit
) {
    val messageContext = MessageContext()
    val messageContextDslBean = MessageContextDslBean()
    block(messageContext, messageContextDslBean)
    val fixId = id.ifBlank { MessageContext::class.java.name }
    this.addContext(fixId, messageContextDslBean.parameters, messageContext)
}