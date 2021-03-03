package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourcePool

class SocketClientPool : ResourcePool<SocketClient>() {
    companion object {
        private val logger = LoggerManager.getLogger(SocketClientPool::class)
    }

    override fun destroyResource(resource: SocketClient?) {
        resource?.close()
    }

    fun <R> useSocketClient(block: (socketClient: SocketClient) -> R?): R? {
        val socketClient = this.stableResource ?: return null
        return try {
            val result = block(socketClient)
            this.releaseStableResource(socketClient)
            result
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
            this.releaseStableResource(socketClient, true)
            null
        }
    }
}