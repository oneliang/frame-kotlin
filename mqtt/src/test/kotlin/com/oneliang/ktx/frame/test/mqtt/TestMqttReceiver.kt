package com.oneliang.ktx.frame.test.mqtt

import com.oneliang.ktx.frame.mqtt.MqttClient
import com.oneliang.ktx.frame.mqtt.MqttReceiver

fun main() {
    val host = "tcp://127.0.0.1:1883"
    val username = "test"
    val password = "test"
    val receiveCallback = object : MqttReceiver.ReceiveCallback {
        override fun onReceived(topic: String, payload: ByteArray) {
            println("after receive, topic:$topic, payload:${String(payload)}")
        }
    }
    val mqttReceiver = MqttReceiver(host, username, password, MqttClient.Option().apply {
//        this.clientId = "stu_app_63_10"
    })
    mqttReceiver.subscribe("test/#", "test/*", object : MqttReceiver.ReceiveCallback {
        override fun onReceived(topic: String, payload: ByteArray) {
            println("$topic,${String(payload)}")
        }

        override fun onError(throwable: Throwable) {
            throwable.printStackTrace()
        }
    })
    mqttReceiver.subscribe("device/+/+", "^device/([\\w]+)/([\\w]+)\$", object : MqttReceiver.ReceiveCallback {
        override fun onReceived(topic: String, payload: ByteArray) {
            println("$topic,${String(payload)}")
        }

        override fun onError(throwable: Throwable) {
            throwable.printStackTrace()
        }
    })
    mqttReceiver.subscribe("\$SYS/brokers/+/clients/#", "^\\\$SYS/brokers/[\\S]+/clients/[\\S]+\$", object : MqttReceiver.ReceiveCallback {
        override fun onReceived(topic: String, payload: ByteArray) {
            println("$topic,${String(payload)}")
        }

        override fun onError(throwable: Throwable) {
            throwable.printStackTrace()
        }
    })
}