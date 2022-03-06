package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.ifNotBlank
import com.oneliang.ktx.util.logging.LoggerManager
import org.fusesource.mqtt.client.FutureConnection
import org.fusesource.mqtt.client.MQTT

object MqttClient {
    private val logger = LoggerManager.getLogger(MqttClient::class)

    fun connect(
        host: String,
        username: String = Constants.String.BLANK,
        password: String = Constants.String.BLANK,
        option: Option? = null,
        afterConnected: (futureConnection: FutureConnection) -> Unit = {}
    ): FutureConnection {
        val mqtt = MQTT()
        mqtt.setHost(host)
        if (username.isNotBlank()) {
            mqtt.setUserName(username)
        }
        if (password.isNotBlank()) {
            mqtt.setPassword(password)
        }
        if (option != null) {
            option.clientId.ifNotBlank { mqtt.setClientId(it) }
            option.willTopic.ifNotBlank { mqtt.setWillTopic(it) }
            option.willMessage.ifNotBlank { mqtt.setWillMessage(it) }
        }
        val connection = mqtt.futureConnection()
        connection.connect().await()
        afterConnected(connection)
        return connection
    }

    class Option {
        var clientId = Constants.String.BLANK
        var willTopic = Constants.String.BLANK
        var willMessage = Constants.String.BLANK
    }
}