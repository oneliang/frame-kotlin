package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import org.fusesource.mqtt.client.BlockingConnection
import org.fusesource.mqtt.client.MQTT

object MqttClient {
    private val logger = LoggerManager.getLogger(MqttClient::class)

    fun connect(host: String, username: String = Constants.String.BLANK, password: String = Constants.String.BLANK, afterConnected: (blockingConnection: BlockingConnection) -> Unit) {
        val mqtt = MQTT()
        mqtt.setHost(host)
        if (username.isNotBlank()) {
            mqtt.setUserName(username)
        }
        if (password.isNotBlank()) {
            mqtt.setPassword(password)
        }
        val connection = mqtt.blockingConnection()
        connection.connect()
        afterConnected(connection)
    }
}