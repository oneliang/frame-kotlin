package com.oneliang.ktx.frame.test.mqtt

import org.fusesource.mqtt.client.MQTT
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic
import java.util.concurrent.TimeUnit

/**
 * A simple MQTT publish and subscribe example.
 */
fun main() { // Create a new MQTT connection to the broker.  We are not setting the client ID.  The broker will pick one for us.
    println("Connecting to Artemis using MQTT")
    val mqtt = MQTT()
    mqtt.setHost("tcp://localhost:1883")
    mqtt.setUserName("test")
    mqtt.setPassword("test")
    val connection = mqtt.blockingConnection()
    connection.connect()
    println("Connected to Artemis")
    // Subscribe to topics
    val topics = arrayOf(Topic("mqtt/example/publish", QoS.AT_LEAST_ONCE), Topic("test/#", QoS.EXACTLY_ONCE), Topic("foo/+/bar", QoS.AT_LEAST_ONCE))
    connection.subscribe(topics)
    println("Subscribed to topics.")
    // Publish Messages
    val payload1 = "This is message 1"
    val payload2 = "This is message 2"
    val payload3 = "This is message 3"
    connection.publish("mqtt/example/publish", payload1.toByteArray(), QoS.AT_LEAST_ONCE, false)
    connection.publish("test/test", payload2.toByteArray(), QoS.AT_MOST_ONCE, false)
    connection.publish("foo/1/bar", payload3.toByteArray(), QoS.AT_MOST_ONCE, false)
    println("Sent messages.")
    Thread.sleep(10000)
    val message1 = connection.receive(5, TimeUnit.SECONDS)
    val message2 = connection.receive(5, TimeUnit.SECONDS)
    val message3 = connection.receive(5, TimeUnit.SECONDS)
    println("Received messages.")
    println(String(message1.payload))
    println(String(message2.payload))
    println(String(message3.payload))
}