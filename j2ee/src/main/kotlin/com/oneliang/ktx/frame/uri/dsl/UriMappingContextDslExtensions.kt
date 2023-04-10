package com.oneliang.ktx.frame.uri.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.uri.UriMappingContext


class UriMappingContextDslBean {
    var parameter: String = Constants.String.BLANK
}

fun ConfigurationContext.uriMappingContext(
    id: String = Constants.String.BLANK,
    block: UriMappingContext.(UriMappingContextDslBean) -> Unit
) {
    val uriMappingContext = UriMappingContext()
    val uriMappingContextDslBean = UriMappingContextDslBean()
    block(uriMappingContext, uriMappingContextDslBean)
    val uriMappingContextParameter = uriMappingContextDslBean.parameter
    val fixId = id.ifBlank { UriMappingContext::class.java.name }
    this.addContext(fixId, uriMappingContextParameter, uriMappingContext)
}