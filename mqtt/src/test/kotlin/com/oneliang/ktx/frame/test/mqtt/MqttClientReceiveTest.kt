package com.oneliang.ktx.frame.test.mqtt

import com.oneliang.ktx.frame.handler.Handler
import com.oneliang.ktx.frame.mqtt.MqttClient
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic

fun main() {
    val host = "tcp://42.192.93.32:1883"
    val username = "test"
    val password = "test"
    val handler = Handler(2, initialize = {
        MqttClient.connect(host, username, password) { connection ->
            val topics = arrayOf(Topic("mqtt/example/publish", QoS.EXACTLY_ONCE), Topic("test/#", QoS.EXACTLY_ONCE), Topic("foo/+/bar", QoS.EXACTLY_ONCE))
            connection.subscribe(topics)
            println("Subscribed to topics.")
            println("Receiving messages.")
        }
    }) { connection ->
        val message = connection.receive()
        message.ack()
        Runnable {
            println(message.topic + "," + String(message.payload))
        }
    }
    handler.start()
//        val message1 = connection.receive(5, TimeUnit.SECONDS)
//        println(String(message1.payload))
//        val message2 = connection.receive(5, TimeUnit.SECONDS)
//        println(String(message2.payload))
//        val message3 = connection.receive(5, TimeUnit.SECONDS)
//        println(String(message3.payload))
    println("Received messages.")
}