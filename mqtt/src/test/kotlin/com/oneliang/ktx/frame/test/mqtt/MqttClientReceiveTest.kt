package com.oneliang.ktx.frame.test.mqtt

import com.oneliang.ktx.frame.handler.ReceiveHandler
import com.oneliang.ktx.frame.mqtt.MqttClient
import org.fusesource.mqtt.client.FutureConnection
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic

fun main() {
    val host = "tcp://127.0.0.1:1883"
    val username = "test"
    val password = "test"
    val receiveHandler = ReceiveHandler(2, initialize = {
        MqttClient.connect(host, username, password) { connection ->
            val topics = arrayOf(Topic("mqtt/example/publish", QoS.EXACTLY_ONCE), Topic("test/#", QoS.EXACTLY_ONCE), Topic("foo/+/bar", QoS.EXACTLY_ONCE))
            connection.subscribe(topics)
            println("Subscribed to topics.")
            println("Receiving messages.")
        }
    }, object : ReceiveHandler.LoopingProcessor<FutureConnection> {
        override fun process(resource: FutureConnection): (FutureConnection) -> Unit {
            val message = resource.receive().await()
            message.ack()
            return {
                println(message.topic + "," + String(message.payload))
            }
        }
    })
    receiveHandler.start()
//        val message1 = connection.receive(5, TimeUnit.SECONDS)
//        println(String(message1.payload))
//        val message2 = connection.receive(5, TimeUnit.SECONDS)
//        println(String(message2.payload))
//        val message3 = connection.receive(5, TimeUnit.SECONDS)
//        println(String(message3.payload))
    println("Received messages.")
}