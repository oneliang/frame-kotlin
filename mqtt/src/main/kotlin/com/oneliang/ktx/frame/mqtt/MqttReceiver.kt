package com.oneliang.ktx.frame.mqtt

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.handler.ReceiveHandler
import com.oneliang.ktx.util.logging.LoggerManager
import org.fusesource.mqtt.client.FutureConnection
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic

class MqttReceiver(
    host: String,
    username: String = Constants.String.BLANK,
    password: String = Constants.String.BLANK,
    option: MqttClient.Option? = null,
    threadCount: Int = 2,
    private val topicArray: Array<String> = emptyArray(),
    private val receiveCallback: ReceiveCallback? = null
) : ReceiveHandler.LoopingProcessor<FutureConnection> {
    companion object {
        private val logger = LoggerManager.getLogger(MqttReceiver::class)
    }

    private val receiveHandler: ReceiveHandler<FutureConnection> = ReceiveHandler(threadCount, initialize = {
        MqttClient.connect(host, username, password, option).also {
            for (topic in topicArray) {
                it.subscribe(arrayOf(Topic(topic, QoS.EXACTLY_ONCE)))
            }
        }
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
            receiveCallback?.afterReceived(topic, payload)
        }
    }

    fun subscribe(topic: String) {
        this.receiveHandler.execute {
            it.subscribe(arrayOf(Topic(topic, QoS.EXACTLY_ONCE)))
        }
    }

    fun unsubscribe(topic: String) {
        this.receiveHandler.execute {
            it.unsubscribe(arrayOf(topic))
        }
    }

    interface ReceiveCallback {
        fun afterReceived(topic: String, payload: ByteArray)
    }
}