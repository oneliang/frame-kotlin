package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.frame.handler.ReceiveHandler
import com.oneliang.ktx.util.logging.LoggerManager
import org.fusesource.mqtt.client.BlockingConnection
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic
import java.util.concurrent.ConcurrentHashMap

class MqttReceiver(host: String, username: String, password: String, threadCount: Int = 2) : ReceiveHandler.LoopingProcessor<BlockingConnection> {
    companion object {
        private val logger = LoggerManager.getLogger(MqttReceiver::class)
    }

    private lateinit var connection: BlockingConnection
    private val receiveHandler = ReceiveHandler(threadCount, initialize = {
        val blockingConnection = MqttClient.connect(host, username, password)
        this.connection = blockingConnection
        blockingConnection
    }, this)

    init {
        this.receiveHandler.start()
    }

    private val topicTaskMap = ConcurrentHashMap<String, (String) -> Unit>()

    @Suppress("UNCHECKED_CAST")
    override fun process(resource: BlockingConnection): (BlockingConnection) -> Unit {
        val message = resource.receive()
        message.ack()
        return {
            val topic = message.topic
            val payload = String(message.payload)
            logger.info("topic:%s, payload:%s", topic, payload)
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