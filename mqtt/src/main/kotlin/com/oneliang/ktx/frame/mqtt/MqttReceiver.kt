package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.handler.ReceiveHandler
import com.oneliang.ktx.util.common.matches
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

    private val topicMatchRegexReceiveCallbackMap = ConcurrentHashMap<String, ReceiveCallback>()

    private val receiveHandler: ReceiveHandler<FutureConnection> = ReceiveHandler(threadCount, initialize = {
        MqttClient.connect(host, username, password, option)
    }, this)

    init {
        logger.verbose("receive handler starting")
        this.receiveHandler.start()
    }

    @Suppress("UNCHECKED_CAST")
    override fun process(resource: FutureConnection): (FutureConnection) -> Unit {
        val message = resource.receive().await()
        message.ack()
        return {
            val topic = message.topic
            val payload = message.payload
            logger.verbose("topic:%s, payload:%s", topic, payload)
            topicMatchRegexReceiveCallbackMap.forEach { (topicMatchRegex, receiveCallback) ->
                if (topic.matches(topicMatchRegex)) {
                    receiveCallback.afterReceived(topic, payload)
                }
            }
        }
    }

    fun subscribe(topic: String, topicMatchRegex: String, receiveCallback: ReceiveCallback) {
        this.subscribe(arrayOf(topic to topicMatchRegex), receiveCallback)
    }

    fun subscribe(topicAndTopicMatchRegexArray: Array<Pair<String, String>>, receiveCallback: ReceiveCallback) {
        this.receiveHandler.execute {
            for ((topic, topicMatchRegex) in topicAndTopicMatchRegexArray) {
                this.topicMatchRegexReceiveCallbackMap[topicMatchRegex] = receiveCallback
                it.subscribe(arrayOf(Topic(topic, QoS.EXACTLY_ONCE)))
            }
        }
    }

    fun unsubscribe(topic: String, topicMatchRegex: String) {
        this.receiveHandler.execute {
            this.topicMatchRegexReceiveCallbackMap.remove(topicMatchRegex)
            it.unsubscribe(arrayOf(topic))
        }
    }

    interface ReceiveCallback {
        fun afterReceived(topic: String, payload: ByteArray)
    }
}