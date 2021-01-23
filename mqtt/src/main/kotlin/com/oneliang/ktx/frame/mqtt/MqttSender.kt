package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.frame.handler.SendHandler
import org.fusesource.mqtt.client.BlockingConnection
import org.fusesource.mqtt.client.QoS

class MqttSender(host: String, username: String, password: String, threadCount: Int = 2) {

    private val sendHandler: SendHandler<BlockingConnection> = SendHandler(threadCount, initialize = {
        MqttClient.connect(host, username, password)
    })

    init {
        this.sendHandler.start()
    }

    fun send(topic: String, data: ByteArray, retain: Boolean) {
        this.sendHandler.execute {
            it.publish(topic, data, QoS.EXACTLY_ONCE, retain)
        }
    }
}