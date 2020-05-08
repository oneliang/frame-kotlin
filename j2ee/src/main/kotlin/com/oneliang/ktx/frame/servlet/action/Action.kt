package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Action {

    @MustBeDocumented
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class RequestMapping(val value: String, val interceptors: Array<Interceptor> = [], val statics: Array<Static> = [], val httpRequestMethods: Array<Constants.Http.RequestMethod> = []) {

        @MustBeDocumented
        @Target(AnnotationTarget.ANNOTATION_CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Interceptor(val id: String, val mode: Mode) {
            enum class Mode {
                BEFORE, AFTER
            }
        }

        @MustBeDocumented
        @Target(AnnotationTarget.ANNOTATION_CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class Static(val parameters: String, val filePath: String)

        @MustBeDocumented
        @Target(AnnotationTarget.VALUE_PARAMETER)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class RequestParameter(val value: String)
    }
}