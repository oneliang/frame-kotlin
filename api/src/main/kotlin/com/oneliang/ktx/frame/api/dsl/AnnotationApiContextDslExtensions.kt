package com.oneliang.ktx.frame.api.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.api.AnnotationApiContext
import com.oneliang.ktx.frame.configuration.ConfigurationContext

class AnnotationApiContextDslBean {
    var parameters: String = Constants.String.BLANK
}

fun ConfigurationContext.annotationApiContext(
    id: String = Constants.String.BLANK,
    block: AnnotationApiContext.(AnnotationApiContextDslBean) -> Unit
) {
    val annotationApiContext = AnnotationApiContext()
    val annotationApiContextDslBean = AnnotationApiContextDslBean()
    block(annotationApiContext, annotationApiContextDslBean)
    val fixId = id.ifBlank { AnnotationApiContext::class.java.name }
    this.addContext(fixId, annotationApiContextDslBean.parameters, annotationApiContext)
}
