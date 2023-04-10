package com.oneliang.ktx.frame.servlet.action.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.servlet.action.AnnotationInterceptorContext

class AnnotationInterceptorContextDslBean {
    var parameter: String = Constants.String.BLANK
}

fun ConfigurationContext.annotationInterceptorContext(
    id: String = Constants.String.BLANK,
    block: AnnotationInterceptorContext.(AnnotationInterceptorContextDslBean) -> Unit
) {
    val annotationInterceptorContext = AnnotationInterceptorContext()
    val annotationInterceptorContextDslBean = AnnotationInterceptorContextDslBean()
    block(annotationInterceptorContext, annotationInterceptorContextDslBean)
    val annotationInterceptorContextParameter = annotationInterceptorContextDslBean.parameter
    val fixId = id.ifBlank { AnnotationInterceptorContext::class.java.name }
    this.addContext(fixId, annotationInterceptorContextParameter, annotationInterceptorContext)
}