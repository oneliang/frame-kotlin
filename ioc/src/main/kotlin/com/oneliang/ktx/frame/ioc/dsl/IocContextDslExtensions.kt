package com.oneliang.ktx.frame.ioc.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.ioc.IocBean
import com.oneliang.ktx.frame.ioc.IocContext

class IocContextDslBean {
    var parameters: String = Constants.String.BLANK
}

fun ConfigurationContext.iocContext(
    id: String = Constants.String.BLANK,
    block: IocContext.(iocContextDslBean: IocContextDslBean) -> Unit
) {
    val iocContext = IocContext()
    val iocContextDslBean = IocContextDslBean()
    block(iocContext, iocContextDslBean)
    val fixId = id.ifBlank { IocContext::class.java.name }
    this.addContext(fixId, iocContextDslBean.parameters, iocContext)
}