package com.oneliang.ktx.frame.ioc

import com.oneliang.ktx.Constants

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ioc(val id: String = Constants.String.BLANK, val proxy: Boolean = true, val injectType: String = IocBean.INJECT_TYPE_AUTO_BY_ID) {

    @MustBeDocumented
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AfterInject
}