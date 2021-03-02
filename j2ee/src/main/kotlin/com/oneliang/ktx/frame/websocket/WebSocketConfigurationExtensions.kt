package com.oneliang.ktx.frame.websocket

import com.oneliang.ktx.frame.configuration.ConfigurationContext

/**
 * before global web socket interceptor list
 */
val ConfigurationContext.beforeGlobalWebSocketInterceptorList: List<WebSocketInterceptorInterface>
    get() {
        var beforeGlobalWebSocketInterceptorList: List<WebSocketInterceptorInterface> = emptyList()
        val annotationWebSocketInterceptorContext = this.findContext(AnnotationWebSocketInterceptorContext::class)
        if (annotationWebSocketInterceptorContext != null) {
            beforeGlobalWebSocketInterceptorList = annotationWebSocketInterceptorContext.getBeforeGlobalWebSocketInterceptorList()
        }
        return beforeGlobalWebSocketInterceptorList
    }