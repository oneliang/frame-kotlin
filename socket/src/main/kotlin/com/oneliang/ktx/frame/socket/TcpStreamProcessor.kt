package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.logging.LoggerManager

abstract class TcpStreamProcessor : StreamProcessor {
    companion object {
        private val logger = LoggerManager.getLogger(TcpStreamProcessor::class)
    }

    val tcpPacketProcessor = TcpPacketProcessor()
}