package com.oneliang.ktx.frame.websocket

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.util.logging.LoggerManager
import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpointConfig

internal object ServerEndpointConfigurator {

    private val logger = LoggerManager.getLogger(ServerEndpointConfigurator::class)


    /**
     * do global interceptor list,include global(before,after)
     * @param webSocketInterceptorList
     * @param handshakeRequest
     * @param handshakeResponse
     * @return WebSocketInterceptorInterface.Result
     */
    private fun doGlobalWebSocketInterceptorList(webSocketInterceptorList: List<WebSocketInterceptorInterface>, handshakeRequest: HandshakeRequest, handshakeResponse: HandshakeResponse): WebSocketInterceptorInterface.Result {
        try {
            for (globalWebSocketInterceptor in webSocketInterceptorList) {
                val result = globalWebSocketInterceptor.intercept(handshakeRequest, handshakeResponse)
                val sign = result.type
                logger.info("Global web socket interceptor, through:%s, web socket interceptor:%s", sign, globalWebSocketInterceptor)
                if (sign != WebSocketInterceptorInterface.Result.Type.NEXT) {
                    return result
                }
            }
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
            return WebSocketInterceptorInterface.Result(WebSocketInterceptorInterface.Result.Type.ERROR)
        }
        return WebSocketInterceptorInterface.Result()
    }

    /**
     * do before global interceptor
     * @param uri
     * @param handshakeRequest
     * @param handshakeResponse
     * @return boolean
     */
    private fun doBeforeGlobalWebSocketInterceptor(uri: String, handshakeRequest: HandshakeRequest, handshakeResponse: HandshakeResponse) {
        //global web socket interceptor doIntercept
        val beforeGlobalWebSocketInterceptorList = ConfigurationContainer.rootConfigurationContext.beforeGlobalWebSocketInterceptorList
        val beforeGlobalWebSocketInterceptorResult = doGlobalWebSocketInterceptorList(beforeGlobalWebSocketInterceptorList, handshakeRequest, handshakeResponse)

        //through the web socket interceptor
        if (beforeGlobalWebSocketInterceptorResult.type == WebSocketInterceptorInterface.Result.Type.ERROR) {
            logger.error("The request name:%s. Can not through the before global web socket interceptors", uri)
            error(beforeGlobalWebSocketInterceptorResult.message)
        }
        logger.info("Through the before global web socket interceptors!")
    }

    @Throws(Throwable::class)
    internal fun modifyHandshake(serverEndpointConfig: ServerEndpointConfig, handshakeRequest: HandshakeRequest, handshakeResponse: HandshakeResponse) {
        val uri = handshakeRequest.requestURI.path
        logger.info("Web socket request uri:%s", uri)
        doBeforeGlobalWebSocketInterceptor(uri, handshakeRequest, handshakeResponse)
    }
}