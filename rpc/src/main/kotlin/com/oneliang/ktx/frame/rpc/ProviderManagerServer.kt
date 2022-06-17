package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.frame.socket.nio.SelectorProcessor
import com.oneliang.ktx.frame.socket.nio.Server
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.concurrent.atomic.LRUCacheSet
import com.oneliang.ktx.util.json.jsonToObject
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

class ProviderManagerServer(host: String = HOST_ADDRESS, port: Int, maxThreadCount: Int = Runtime.getRuntime().availableProcessors()) {

    companion object {
        private val logger = LoggerManager.getLogger(ProviderManagerServer::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val clusterHostMap = ConcurrentHashMap<String, LRUCacheSet<Provider>>()

    private val selectorProcessor: SelectorProcessor = object : SelectorProcessor {
        override fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray {
            val tlvPacket = tlvPacketProcessor.receiveTlvPacket(ByteArrayInputStream(byteArray))
            val requestString = String(tlvPacket.body)
            logger.debug("server read:%s", requestString)
            val baseData = requestString.jsonToObject(BaseData::class)
            val action = baseData.action
            val id = baseData.id
            return when (action) {
                ConstantsRemoteProcessCall.Action.LOOKUP_PROVIDER -> {
                    logger.debug("begin lookup host, id:%s", id)
                    val lookupProviderRequest = requestString.jsonToObject(LookupProviderRequest::class)
                    val clusterKey = lookupProviderRequest.clusterKey
                    val servers = clusterHostMap[clusterKey]
                    val lookupProviderResponse = LookupProviderResponse.build(id, true)
                    val provider = if (servers != null && servers.size > 0) servers.first() else null
                    if (servers != null && provider != null) {
                        servers.operate(provider)//update lru counter
                    }
                    lookupProviderResponse.provider = provider
                    val lookupResponseJson = lookupProviderResponse.toJson()
                    logger.debug("end lookup host, json:%s", lookupResponseJson)
                    TlvPacket(ConstantsRemoteProcessCall.TlvPackageType.LOOKUP_PROVIDER, lookupResponseJson.toByteArray()).toByteArray()
                }
                ConstantsRemoteProcessCall.Action.REGISTER -> {
                    val providerRegisterRequest = requestString.jsonToObject(ProviderRegisterRequest::class)
                    val clusterKey = providerRegisterRequest.clusterKey
                    val providerHost = providerRegisterRequest.host
                    val providerPort = providerRegisterRequest.port
                    val providerSet = clusterHostMap.getOrPut(clusterKey) { LRUCacheSet(type = LRUCacheSet.Type.ASCENT) }
                    providerSet.operate(Provider(providerHost, providerPort))
                    val providerRegisterResponseJson = ProviderRegisterResponse.build(id, true).toJson()
                    TlvPacket(ConstantsRemoteProcessCall.TlvPackageType.REGISTER, providerRegisterResponseJson.toByteArray()).toByteArray()
                }
                else -> {
                    val lookupResponseJson = LookupProviderResponse.build(id, false).also { it.action = ConstantsRemoteProcessCall.Action.NONE }.toJson()
                    TlvPacket(ConstantsRemoteProcessCall.TlvPackageType.NONE, lookupResponseJson.toByteArray()).toByteArray()
                }
            }
        }
    }
    private val server = Server(host, port, maxThreadCount).also { it.selectorProcessor = this.selectorProcessor }

    fun start() {
        this.server.start()
    }

    fun stop() {
        this.clusterHostMap.clear()
        this.server.stop()
    }
}