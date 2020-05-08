package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.frame.configuration.ConfigurationContext
import kotlin.reflect.KClass

/**
 * find mappingBean
 *
 * @param <T>
 * @param kClass
 * @return MappingBean
</T> */
fun <T : Any> ConfigurationContext.findMappingBean(kClass: KClass<T>): MappingBean? {
    val mappingContext = this.findContext(MappingContext::class)
    return mappingContext?.findMappingBean(kClass)
}

/**
 * find mappingBean
 *
 * @param name
 * @return MappingBean
 */
fun ConfigurationContext.findMappingBean(name: String): MappingBean? {
    val mappingContext = this.findContext(MappingContext::class)
    return mappingContext?.findMappingBean(name)
}