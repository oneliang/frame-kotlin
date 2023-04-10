package com.oneliang.ktx.frame.servlet.action.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.AnnotationActionContext

class AnnotationActionContextDslBean {
    var parameter: String = Constants.String.BLANK
}

fun ConfigurationContext.annotationActionContext(
    id: String = Constants.String.BLANK,
    block: AnnotationActionContext.(AnnotationActionContextDslBean) -> Unit
) {
    val annotationActionContext = AnnotationActionContext()
    val annotationActionContextDslBean = AnnotationActionContextDslBean()
    block(annotationActionContext, annotationActionContextDslBean)
    val annotationActionContextParameter = annotationActionContextDslBean.parameter
    val fixId = id.ifBlank { AnnotationActionContext::class.java.name }
    this.addContext(fixId, annotationActionContextParameter, annotationActionContext)
}
