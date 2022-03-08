package com.oneliang.ktx.frame.test.mqtt

import com.oneliang.ktx.frame.mqtt.MqttReceiver

fun main() {
    val host = "tcp://127.0.0.1:1883"
    val username = "test"
    val password = "test"
    val receiveCallback = object : MqttReceiver.ReceiveCallback {
        override fun afterReceived(topic: String, payload: ByteArray) {
            println("after receive, topic:$topic, payload:${String(payload)}")
        }
    }
    val mqttReceiver = MqttReceiver(host, username, password)
    mqttReceiver.receiveCallback = receiveCallback
    mqttReceiver.subscribe("mqtt/#")
}