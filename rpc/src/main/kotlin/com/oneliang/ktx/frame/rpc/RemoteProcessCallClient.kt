package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.socket.nio.ClientManager
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.PID
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacketProcessor

class RemoteProcessCallClient(
    providerServerHost: String,
    providerServerPort: Int,
    private val readProcessor: (byteArray: ByteArray) -> Unit = {}
) {
    companion object {
        private val logger = LoggerManager.getLogger(RemoteProcessCallClient::class)
        private val tlvPacketProcessor = TlvPacketProcessor()
    }

    private val providerClient = ProviderClient(providerServerHost, providerServerPort)
    private var clientManager: ClientManager? = null

    private fun generateGlobalThreadId(): String {
        val tid = Thread.currentThread().id.toString()
        //tid@pid@hostAddress, threadId+jvmId+IP
        return tid + Constants.Symbol.AT + PID + Constants.Symbol.AT + HOST_ADDRESS
    }

    fun lookup(clusterKey: String): Boolean {
        val server = this.providerClient.lookup(clusterKey)
        if (server != null) {
            logger.info("Lookup a server success, host:%s, port:%s", server.host, server.port)
        } else {
            logger.error("Lookup a server failure, but it is null. Can not found a suitable server.")
            return false
        }
        this.clientManager = ClientManager(server.host, server.port, 1, readProcessor = this.readProcessor)
        this.clientManager?.start()
        return true
    }

    fun remoteProcessCall(method: String, parameters: Array<ByteArray>) {
        val remoteProcessCallRequest = RemoteProcessCallRequest()
        remoteProcessCallRequest.id = generateGlobalThreadId()
        remoteProcessCallRequest.method = method
        remoteProcessCallRequest.parameters = parameters
        this.clientManager?.send(remoteProcessCallRequest.toByteArray())

    }

    fun send(byteArray: ByteArray) {
        this.clientManager?.send(byteArray)
    }
}