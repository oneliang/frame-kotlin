package com.oneliang.ktx.frame.bus

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.concurrent.ResourceQueueThread
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class EventBus(private val threadCount: Int = 1) {

    private val coroutine = Coroutine(Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher())
    private val resourceQueueThread = ResourceQueueThread(object : ResourceQueueThread.ResourceProcessor<EventMessage> {
        override fun process(resource: EventMessage) {
            coroutine.launch {
                resource.eventHandler.handle(resource.event, resource.message)
            }
        }
    })

    interface EventHandler {
        fun handle(event: String, message: Any?)
    }

    private class EventMessage(val event: String, val message: Any? = null, val eventHandler: EventHandler)

    @Synchronized
    fun start() {
        this.resourceQueueThread.start()
    }

    fun addEvent(event: String, message: Any? = null, eventHandler: EventHandler) {
        this.resourceQueueThread.addResource(EventMessage(event, message, eventHandler))
    }

    @Synchronized
    fun interrupt() {
        this.resourceQueueThread.interrupt()
    }
}