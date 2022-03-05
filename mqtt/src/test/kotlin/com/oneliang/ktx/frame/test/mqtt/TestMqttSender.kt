package com.oneliang.ktx.frame.test.mqtt

import com.oneliang.ktx.frame.mqtt.MqttSender

fun main() {
    val host = "tcp://127.0.0.1:1883"
    val username = "test"
    val password = "test"
    val mqttSender = MqttSender(host, username, password)
    for (i in 1..10) {
        mqttSender.send("mqtt/example/publish", "payload:$i".toByteArray())
        Thread.sleep(1000)
    }
}