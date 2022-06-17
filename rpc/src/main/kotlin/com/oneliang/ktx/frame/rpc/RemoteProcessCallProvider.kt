package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.logging.LoggerManager

class RemoteProcessCallProvider(
    providerManagerServerHost: String,
    providerManagerServerPort: Int,
    clusterKey: String,
    host: String = HOST_ADDRESS,
    port: Int,
    processor: Processor
) {
    companion object {
        private val logger = LoggerManager.getLogger(RemoteProcessCallProvider::class)
    }

    private val providerTrafficStat = ProviderTrafficStat()
    private val providerClient = ProviderClient(providerManagerServerHost, providerManagerServerPort)
    private val selectorProcessor: SelectorProcessor = object : SelectorProcessor {
        override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
            val dataLength = byteArray.size.toLong()
            providerTrafficStat.add(dataLength)
            providerTrafficStat.minus(dataLength)
            val remoteProcessCallRequest = RemoteProcessCallRequest.fromByteArray(byteArray)
            logger.debug("remote process call request, id:%s, method:%s, parameter size:%s", remoteProcessCallRequest.id, remoteProcessCallRequest.method, remoteProcessCallRequest.parameters.size)
            val result = processor.process(remoteProcessCallRequest.method, remoteProcessCallRequest.parameters)
            val remoteProcessCallResponse = RemoteProcessCallResponse.build(remoteProcessCallRequest.id, remoteProcessCallRequest.method, true, result)
            return remoteProcessCallResponse.toByteArray()
        }
    }
    private val server = Server(host, port).also { it.selectorProcessor = this.selectorProcessor }

    init {
        this.server.start()
        this.providerTrafficStat.start()
        this.providerClient.register(clusterKey, host, port)
    }

    interface Processor {
        fun process(method: String, parameters: Array<ByteArray>): ByteArray
    }
}

