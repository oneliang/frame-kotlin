package com.oneliang.ktx.frame.websocket

import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest

interface WebSocketInterceptorInterface {
    /**
     * through intercept return true,else return false
     * @param handshakeRequest
     * @param handshakeResponse
     * @return boolean
     * @exception InterceptException
     */
    @Throws(WebSocketInterceptorInterface.InterceptException::class)
    fun intercept(handshakeRequest: HandshakeRequest, handshakeResponse: HandshakeResponse): Result

    class Result(val type: Type = Type.NEXT, val message: ByteArray = ByteArray(0)) {
        enum class Type {
            NEXT, ERROR
        }
    }

    class InterceptException : Exception {

        /**
         * @param message
         */
        constructor(message: String) : super(message)

        /**
         * @param cause
         */
        constructor(cause: Throwable) : super(cause)

        /**
         * @param message
         * @param cause
         */
        constructor(message: String, cause: Throwable) : super(message, cause)
    }
}
