package com.oneliang.ktx.frame.ioc.aop

/**
 * annotation DataCache for the method of no arguments
 * @author Dandelion
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DataCache(val updateTime: Long = 10000)
