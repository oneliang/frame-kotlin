package com.oneliang.ktx.frame.handler

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class SendHandler<T : Any>(
    threadCount: Int = 1,
    initialize: () -> T
) : Handler<T> {
    companion object {
        private val logger = LoggerManager.getLogger(SendHandler::class)
    }

    private val executorService = Executors.newFixedThreadPool(threadCount)
    private val coroutine = Coroutine(executorService.asCoroutineDispatcher())
    private lateinit var resource: T

    init {
        if (!this::resource.isInitialized) {
            this.resource = initialize()
        }
    }

    override fun execute(task: (T) -> Unit) {
        this.coroutine.launch {
            task(this.resource)
        }
    }

    fun shutdown() {
        this.executorService.shutdown()
    }
}