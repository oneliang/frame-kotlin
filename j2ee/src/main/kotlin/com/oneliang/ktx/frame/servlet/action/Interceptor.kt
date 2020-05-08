package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Interceptor(val id: String = Constants.String.BLANK, val mode: Mode = Mode.SINGLE_ACTION) {

    enum class Mode {
        GLOBAL_ACTION_BEFORE, GLOBAL_ACTION_AFTER, SINGLE_ACTION
    }
}
