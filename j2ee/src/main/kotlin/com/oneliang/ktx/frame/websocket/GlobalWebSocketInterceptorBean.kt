package com.oneliang.ktx.frame.websocket

import com.oneliang.ktx.Constants

class GlobalWebSocketInterceptorBean {

    companion object {
        const val TAG_GLOBAL_WEB_SOCKET_INTERCEPTOR = "global-web-socket-interceptor"
        const val INTERCEPTOR_MODE_BEFORE = "before"
    }

    var id: String = Constants.String.BLANK
    var type: String? = null
    var mode: String = INTERCEPTOR_MODE_BEFORE
    var interceptorInstance: WebSocketInterceptorInterface? = null
}
