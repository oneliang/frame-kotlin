package com.oneliang.ktx.frame.handler

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class SendHandler<T : Any>(
    threadCount: Int = 1,
    initialize: () -> T
) {
    companion object {
        private val logger = LoggerManager.getLogger(SendHandler::class)
    }

    private val coroutine = Coroutine(Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher())
    private lateinit var resource: T

    init {
        if (!this::resource.isInitialized) {
            this.resource = initialize()
        }
    }

    fun execute(handle: (T) -> Unit) {
        this.coroutine.launch {
            handle(this.resource)
        }
    }
}