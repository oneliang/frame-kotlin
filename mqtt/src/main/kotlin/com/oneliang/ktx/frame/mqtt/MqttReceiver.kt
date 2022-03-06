package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.handler.ReceiveHandler
import com.oneliang.ktx.util.logging.LoggerManager
import org.fusesource.mqtt.client.FutureConnection
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic
import java.util.concurrent.ConcurrentHashMap

class MqttReceiver(
    host: String,
    username: String = Constants.String.BLANK,
    password: String = Constants.String.BLANK,
    option: MqttClient.Option? = null,
    threadCount: Int = 2
) : ReceiveHandler.LoopingProcessor<FutureConnection> {
    companion object {
        private val logger = LoggerManager.getLogger(MqttReceiver::class)
    }

    private val receiveHandler: ReceiveHandler<FutureConnection> = ReceiveHandler(threadCount, initialize = {
        MqttClient.connect(host, username, password, option)
    }, this)

    init {
        logger.verbose("receive handler starting")
        this.receiveHandler.start()
    }

    private val topicTaskMap = ConcurrentHashMap<String, (topic: String, payload: ByteArray) -> Unit>()

    @Suppress("UNCHECKED_CAST")
    override fun process(resource: FutureConnection): (FutureConnection) -> Unit {
        val message = resource.receive().await()
        message.ack()
        return {
            val topic = message.topic
            val payload = message.payload
            logger.verbose("topic:%s, payload:%s", topic, payload)
            this.topicTaskMap[topic]?.invoke(topic, payload)
        }
    }

    fun addReceiveTask(topic: String, task: (topic: String, payload: ByteArray) -> Unit) {
        this.topicTaskMap[topic] = task
        this.receiveHandler.execute {
            it.subscribe(arrayOf(Topic(topic, QoS.EXACTLY_ONCE)))
        }
    }

    fun removeReceiveTask(topic: String) {
        this.topicTaskMap.remove(topic)
        this.receiveHandler.execute {
            it.unsubscribe(arrayOf(topic))
        }
    }
}