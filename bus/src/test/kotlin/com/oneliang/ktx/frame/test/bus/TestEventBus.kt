package com.oneliang.ktx.frame.test.bus

import com.oneliang.ktx.frame.bus.EventBus

fun main() {
    val eventBus = EventBus()
    eventBus.start()
    eventBus.addEvent("a", eventHandler = object : EventBus.EventHandler {
        override fun handle(event: String, message: Any?) {
            println(event)
        }
    })
}