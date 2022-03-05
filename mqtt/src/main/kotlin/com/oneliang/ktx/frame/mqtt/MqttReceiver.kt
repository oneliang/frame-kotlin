package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.frame.handler.ReceiveHandler
import com.oneliang.ktx.util.logging.LoggerManager
import org.fusesource.mqtt.client.FutureConnection
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic
import java.util.concurrent.ConcurrentHashMap

class MqttReceiver(host: String, username: String, password: String, threadCount: Int = 2) : ReceiveHandler.LoopingProcessor<FutureConnection> {
    companion object {
        private val logger = LoggerManager.getLogger(MqttReceiver::class)
    }

    private lateinit var connection: FutureConnection
    private val receiveHandler: ReceiveHandler<FutureConnection> = ReceiveHandler(threadCount, initialize = {
        val futureConnection = MqttClient.connect(host, username, password)
        this.connection = futureConnection
        futureConnection
    }, this)

    init {
        logger.verbose("receive handler starting")
        this.receiveHandler.start()
    }

    private val topicTaskMap = ConcurrentHashMap<String, (String) -> Unit>()

    @Suppress("UNCHECKED_CAST")
    override fun process(resource: FutureConnection): (FutureConnection) -> Unit {
        val message = resource.receive().await()
        message.ack()
        return {
            val topic = message.topic
            val payload = String(message.payload)
            logger.verbose("topic:%s, payload:%s", topic, payload)
            this.topicTaskMap[topic]?.invoke(payload)
        }
    }

    fun addReceiveTask(topic: String, task: (String) -> Unit) {
        this.topicTaskMap[topic] = task
        if (this::connection.isInitialized) {
            this.connection.subscribe(arrayOf(Topic(topic, QoS.EXACTLY_ONCE)))
        }
    }

    fun removeReceiveTask(topic: String) {
        this.topicTaskMap.remove(topic)
        if (this::connection.isInitialized) {
            this.connection.unsubscribe(arrayOf(topic))
        }
    }
}