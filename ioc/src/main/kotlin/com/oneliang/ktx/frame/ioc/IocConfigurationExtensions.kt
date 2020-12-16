package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.configuration.ConfigurationContext

/**
 * ioc inject
 */
@Throws(Exception::class)
fun ConfigurationContext.iocInject() {
    this.findContext(IocContext::class) {
        it.inject()
    }
}

/**
 * put to ioc bean map and auto inject object by id
 * @param id
 * @param instance
 * @throws Exception
 */
@Throws(Exception::class)
fun ConfigurationContext.putToIocBeanMapAndAutoInjectObjectById(id: String, instance: Any) {
    this.findContext(IocContext::class) {
        val iocBean = IocBean.build(id, instance)
        it.putToIocBeanMap(iocBean)
        it.autoInjectObjectById(id, instance)
    }
}

/**
 * auto inject object by id
 * @param id
 * @param instance
 * @throws Exception
 */
@Throws(Exception::class)
fun ConfigurationContext.autoInjectObjectById(id: String, instance: Any) {
    this.findContext(IocContext::class) {
        it.autoInjectObjectById(id, instance)
    }
}

/**
 * auto inject object by type
 * @param id
 * @param instance
 * @throws Exception
 */
@Throws(Exception::class)
fun ConfigurationContext.autoInjectObjectByType(id: String, instance: Any) {
    this.findContext(IocContext::class) {
        it.autoInjectObjectByType(id, instance)
    }
}

/**
 * put object to ioc bean map
 * @param id
 * @param instance
 */
fun ConfigurationContext.putObjectToIocBeanMap(id: String, instance: Any) {
    this.findContext(IocContext::class) {
        val iocBean = IocBean.build(id, instance)
        it.putToIocBeanMap(iocBean)
    }
}

/**
 * after inject
 */
@Throws(Exception::class)
fun ConfigurationContext.afterInject() {
    this.findContext(IocContext::class) {
        it.afterInject()
    }
}


/**
 * auto inject by id
 */
@Throws(Exception::class)
fun <T : Any> T.autoInjectById() {
    ConfigurationContainer.rootConfigurationContext.autoInjectObjectById(this::class.java.name, this)
}

/**
 * auto inject by type
 */
@Throws(Exception::class)
fun <T : Any> T.autoInjectByType() {
    ConfigurationContainer.rootConfigurationContext.autoInjectObjectByType(this::class.java.name, this)
}

/**
 * auto inject by type
 */
@Throws(Exception::class)
fun <T> ConfigurationContext.explicitInvoke(methodId: String, vararg args: Any?): T? {
    val iocContext = this.findContext(IocContext::class)
    return iocContext?.explicitInvoke(methodId, *args)
}