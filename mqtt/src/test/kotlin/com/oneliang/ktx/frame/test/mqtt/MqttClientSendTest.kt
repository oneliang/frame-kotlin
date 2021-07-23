package com.oneliang.ktx.frame.test.mqtt

import com.oneliang.ktx.frame.mqtt.MqttClient
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic

fun main() {
    val host = ""
    val username = "test"
    val password = "test"
    MqttClient.connect(host, username, password) { connection ->
        // Publish Messages
        val payload1 = "This is message 4"
        val payload2 = "This is message 5"
        val payload3 = "This is message 6"
        connection.publish("mqtt/example/publish", payload1.toByteArray(), QoS.EXACTLY_ONCE, true)
        connection.publish("test/test", payload2.toByteArray(), QoS.EXACTLY_ONCE, true)
        connection.publish("foo/1/bar", payload3.toByteArray(), QoS.EXACTLY_ONCE, true)
    }
}