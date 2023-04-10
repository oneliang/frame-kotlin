package com.oneliang.ktx.frame.configuration.dsl

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.configuration.ConfigurationBean
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.configuration.ConfigurationContext

fun configurationContext(block: ConfigurationContext.() -> Unit) {
    ConfigurationContainer.rootConfigurationContext.initialize(Constants.String.BLANK)
    block(ConfigurationContainer.rootConfigurationContext)
}

fun ConfigurationContext.configuration(id: String, block: ConfigurationBean.() -> Unit) {
    id.ifBlank { throw InitializeException("configuration.id can not be blank") }
    val configurationBean = ConfigurationBean()
    configurationBean.id = id
    block(configurationBean)
    configurationBean.parameters.ifBlank { throw InitializeException("configuration.parameters can not be blank") }
    configurationBean.contextClass.ifBlank { throw InitializeException("configuration.contextClass can not be blank") }
    this.addContext(configurationBean)
}
