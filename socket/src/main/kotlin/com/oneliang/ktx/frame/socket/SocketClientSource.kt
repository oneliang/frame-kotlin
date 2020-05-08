package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.resource.ResourceSource

class SocketClientSource : ResourceSource<SocketClient>() {

    var host: String = Constants.String.BLANK
    var port: Int = 0

    lateinit var tcpPacketProcessor: TcpPacketProcessor

    override val resource: SocketClient?
        get() = SocketClient(this.host, this.port).also {
            it.tcpPacketProcessor = tcpPacketProcessor
        }
}