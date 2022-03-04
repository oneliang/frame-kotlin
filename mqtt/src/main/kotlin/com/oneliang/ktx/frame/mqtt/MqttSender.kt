package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.frame.handler.SendHandler
import com.oneliang.ktx.util.logging.LoggerManager
import org.fusesource.mqtt.client.BlockingConnection
import org.fusesource.mqtt.client.QoS

class MqttSender(host: String, username: String, password: String, threadCount: Int = 2) {
    companion object {
        private val logger = LoggerManager.getLogger(MqttSender::class)
    }

    private val sendHandler: SendHandler<BlockingConnection> = SendHandler(threadCount, initialize = {
        MqttClient.connect(host, username, password)
    })

    init {
        logger.info("send handler starting")
    }

    fun send(topic: String, data: ByteArray, retain: Boolean) {
        this.sendHandler.execute {
            it.publish(topic, data, QoS.EXACTLY_ONCE, retain)
        }
    }
}