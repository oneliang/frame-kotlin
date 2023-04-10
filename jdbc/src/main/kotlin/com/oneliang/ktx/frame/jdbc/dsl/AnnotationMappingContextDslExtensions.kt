package com.oneliang.ktx.frame.jdbc.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.jdbc.AnnotationMappingContext

class AnnotationMappingContextDslBean {
    var parameter: String = Constants.String.BLANK
}

fun ConfigurationContext.annotationMappingContext(
    id: String = Constants.String.BLANK,
    block: AnnotationMappingContext.(AnnotationMappingContextDslBean) -> Unit
) {
    val annotationMappingContext = AnnotationMappingContext()
    val annotationMappingContextDslBean = AnnotationMappingContextDslBean()
    block(annotationMappingContext, annotationMappingContextDslBean)
    val annotationMappingContextParameter = annotationMappingContextDslBean.parameter
    val fixId = id.ifBlank { AnnotationMappingContext::class.java.name }
    this.addContext(fixId, annotationMappingContextParameter, annotationMappingContext)
}