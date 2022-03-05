package com.oneliang.ktx.frame.test.mqtt

import com.oneliang.ktx.frame.mqtt.MqttReceiver

fun main() {
    val host = "tcp://127.0.0.1:1883"
    val username = "test"
    val password = "test"
    val mqttReceiver = MqttReceiver(host, username, password)
    mqttReceiver.addReceiveTask("mqtt/example/publish") {
        println("after receive:$it")
    }
    mqttReceiver.addReceiveTask("test/test") {
        println("after receive:$it")
    }
    mqttReceiver.addReceiveTask("foo/1/bar") {
        println("after receive:$it")
    }
}