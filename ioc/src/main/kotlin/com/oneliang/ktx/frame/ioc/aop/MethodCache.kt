package com.oneliang.ktx.frame.ioc.aop

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class MethodCache(val cacheRefreshTime: Long = 0L) {
    //val listFields: Array<ListField> = []) {

    @MustBeDocumented
    @Target(AnnotationTarget.ANNOTATION_CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ListField(val field: String, val kClass: KClass<*>)
}