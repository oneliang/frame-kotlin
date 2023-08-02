package com.oneliang.ktx.frame.ioc.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.ioc.AnnotationIocContext

class AnnotationIocContextDslBean {
    var parameters: String = Constants.String.BLANK
}

fun ConfigurationContext.annotationIocContext(
    id: String = Constants.String.BLANK,
    block: AnnotationIocContext.(annotationIocContextDslBean: AnnotationIocContextDslBean) -> Unit
) {
    val annotationIocContext = AnnotationIocContext()
    val annotationIocContextDslBean = AnnotationIocContextDslBean()
    block(annotationIocContext, annotationIocContextDslBean)
    val fixId = id.ifBlank { AnnotationIocContext::class.java.name }
    this.addContext(fixId, annotationIocContextDslBean.parameters, annotationIocContext)
}