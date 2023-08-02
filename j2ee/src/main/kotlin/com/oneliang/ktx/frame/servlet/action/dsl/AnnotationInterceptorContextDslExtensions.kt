package com.oneliang.ktx.frame.servlet.action.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.AnnotationInterceptorContext

class AnnotationInterceptorContextDslBean {
    var parameters: String = Constants.String.BLANK
}

fun ConfigurationContext.annotationInterceptorContext(
    id: String = Constants.String.BLANK,
    block: AnnotationInterceptorContext.(AnnotationInterceptorContextDslBean) -> Unit
) {
    val annotationInterceptorContext = AnnotationInterceptorContext()
    val annotationInterceptorContextDslBean = AnnotationInterceptorContextDslBean()
    block(annotationInterceptorContext, annotationInterceptorContextDslBean)
    val fixId = id.ifBlank { AnnotationInterceptorContext::class.java.name }
    this.addContext(fixId, annotationInterceptorContextDslBean.parameters, annotationInterceptorContext)
}