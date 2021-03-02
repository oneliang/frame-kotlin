package com.oneliang.ktx.frame.websocket

import com.oneliang.ktx.Constants

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class WebSocketInterceptor(val id: String = Constants.String.BLANK, val mode: Mode = Mode.GLOBAL_ACTION_BEFORE) {

    enum class Mode {
        GLOBAL_ACTION_BEFORE
    }
}
