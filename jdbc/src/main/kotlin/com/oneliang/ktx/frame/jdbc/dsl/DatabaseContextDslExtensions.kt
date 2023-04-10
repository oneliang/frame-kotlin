package com.oneliang.ktx.frame.jdbc.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.frame.jdbc.DatabaseContext

class DatabaseContextDslBean {
    var parameter: String = Constants.String.BLANK
}

fun ConfigurationContext.databaseContext(
    id: String = Constants.String.BLANK,
    block: DatabaseContext.(DatabaseContextDslBean) -> Unit
) {
    val databaseContext = DatabaseContext()
    val databaseContextDslBean = DatabaseContextDslBean()
    block(databaseContext, databaseContextDslBean)
    val databaseContextParameter = databaseContextDslBean.parameter
    val fixId = id.ifBlank { DatabaseContext::class.java.name }
    this.addContext(fixId, databaseContextParameter, databaseContext)
}