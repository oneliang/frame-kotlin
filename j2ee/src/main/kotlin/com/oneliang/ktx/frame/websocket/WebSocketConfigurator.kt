package com.oneliang.ktx.frame.websocket

import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpointConfig

class WebSocketConfigurator : ServerEndpointConfig.Configurator() {
    override fun modifyHandshake(serverEndpointConfig: ServerEndpointConfig, handshakeRequest: HandshakeRequest, handshakeResponse: HandshakeResponse) {
        ServerEndpointConfigurator.modifyHandshake(serverEndpointConfig, handshakeRequest, handshakeResponse)
    }
}