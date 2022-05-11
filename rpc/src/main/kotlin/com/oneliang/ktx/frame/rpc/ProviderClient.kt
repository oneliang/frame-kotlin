package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.PID
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.concurrent.atomic.AwaitAndSignal
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.util.concurrent.ConcurrentHashMap

class ProviderClient(serverHost: String, serverPort: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(ProviderClient::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val lookupProviderResponseDataMap = ConcurrentHashMap<String, LookupProviderResponse>()
    private val providerRegisterResponseDataMap = ConcurrentHashMap<String, ProviderRegisterResponse>()
    private val awaitAndSignal = AwaitAndSignal<String>()

    //second way:clientManager readProcessor is lambda, so you can implement Function interface to use it
    private val readProcessor = { byteArray: ByteArray ->
        val responseTlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
        val responseJson = String(responseTlvPacket.body)
        logger.debug("receive, type:%s, body json:%s", responseTlvPacket.type.toInt(), responseJson)
        val baseData = responseJson.jsonToObject(BaseData::class)
        val id = baseData.id
        val action = baseData.action
        if (action == ConstantsRemoteProcessCall.Action.LOOKUP_PROVIDER) {
            val lookupProviderResponse = responseJson.jsonToObject(LookupProviderResponse::class)
            this.lookupProviderResponseDataMap[id] = lookupProviderResponse
            //active thread and thread will read the data
            this.awaitAndSignal.signal(id)
        } else if (action == ConstantsRemoteProcessCall.Action.REGISTER) {
            val providerRegisterResponse = responseJson.jsonToObject(ProviderRegisterResponse::class)
            this.providerRegisterResponseDataMap[id] = providerRegisterResponse
            //active thread and thread will read the data
            this.awaitAndSignal.signal(id)
        }
    }

    private val clientManager = ClientManager(serverHost, serverPort, readProcessor = this.readProcessor)

    init {
        this.clientManager.start()
    }

    private fun generateGlobalThreadId(): String {
        val tid = Thread.currentThread().id.toString()
        //tid@pid@hostAddress, threadId+jvmId+IP
        return tid + Constants.Symbol.AT + PID + Constants.Symbol.AT + HOST_ADDRESS
    }

    fun register(clusterKey: String, host: String, port: Int): Boolean {
        val id = generateGlobalThreadId()
        val providerRegisterRequestJson = ProviderRegisterRequest.build(id, clusterKey, host, port).toJson()
        logger.debug("provider register request:%s", providerRegisterRequestJson)
        val registerTlvPacket = TlvPacket(ConstantsRemoteProcessCall.TlvPackageType.REGISTER, providerRegisterRequestJson.toByteArray())
        this.clientManager.send(registerTlvPacket.toByteArray())
        this.awaitAndSignal.await(id)
        val providerRegisterResponse = this.providerRegisterResponseDataMap.remove(id)
        logger.debug("id:%s, provider register success:%s", id, providerRegisterResponse?.success)
        return providerRegisterResponse?.success ?: false
    }

    fun lookup(clusterKey: String): Provider? {
        val id = generateGlobalThreadId()
        val lookupProviderRequestJson = LookupProviderRequest.build(id, clusterKey).toJson()
        logger.debug("lookup provider request:%s", lookupProviderRequestJson)
        val lookupTlvPacket = TlvPacket(ConstantsRemoteProcessCall.TlvPackageType.LOOKUP_PROVIDER, lookupProviderRequestJson.toByteArray())
        this.clientManager.send(lookupTlvPacket.toByteArray())
        this.awaitAndSignal.await(id)
        val lookupProviderResponse = this.lookupProviderResponseDataMap.remove(id)
        var provider: Provider? = null
        if (lookupProviderResponse != null) {
            val action = lookupProviderResponse.action
            val responseId = lookupProviderResponse.id
            if (id != responseId) {
                error("it is impossible, it maybe logic error")
            }
            val success = lookupProviderResponse.success
            when (action) {
                ConstantsRemoteProcessCall.Action.LOOKUP_PROVIDER -> {
                    if (success) {
                        provider = lookupProviderResponse.provider
                    } else {
                        logger.error("lookup response is failure? it maybe has no available servers, id:%s, server key:%s", id, clusterKey)
                    }
                }
            }
        } else {
            logger.error("lookup response is null? it maybe logic error, id:%s", id)
        }
        return provider
    }
}
