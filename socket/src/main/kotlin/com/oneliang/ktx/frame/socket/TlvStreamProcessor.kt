package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacketProcessor

abstract class TlvStreamProcessor : StreamProcessor {
    companion object {
        private val logger = LoggerManager.getLogger(TlvStreamProcessor::class)
    }

    val tlvPacketProcessor = TlvPacketProcessor()
}