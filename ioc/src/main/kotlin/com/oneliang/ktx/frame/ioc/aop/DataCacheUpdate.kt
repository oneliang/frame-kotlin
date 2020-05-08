package com.oneliang.ktx.frame.ioc.aop

/**
 * annotation DataCacheUpdate for the method of the last arguments which is data cache
 * @author Dandelion
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DataCacheUpdate(val dataCacheMethod: String)
