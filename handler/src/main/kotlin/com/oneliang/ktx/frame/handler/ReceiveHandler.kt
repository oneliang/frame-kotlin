package com.oneliang.ktx.frame.handler

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class ReceiveHandler<T : Any>(
    threadCount: Int = 1,
    initialize: () -> T,
    private val loopingProcessor: LoopingProcessor<T>
) : LoopThread(), Handler<T> {
    companion object {
        private val logger = LoggerManager.getLogger(ReceiveHandler::class)
    }

    private val executorService = Executors.newFixedThreadPool(threadCount)
    private val coroutine = Coroutine(this.executorService.asCoroutineDispatcher())
    private lateinit var resource: T

    init {
        if (!this::resource.isInitialized) {
            this.resource = initialize()
        }
    }

    override fun looping() {
        val task = this.loopingProcessor.process(this.resource)
        execute(task)
    }

    override fun execute(task: (T) -> Unit) {
        this.coroutine.launch {
            task.invoke(this.resource)
        }
    }

    override fun stop() {
        this.executorService.shutdown()
        super.stop()
    }

    interface LoopingProcessor<T : Any> {
        fun process(resource: T): (T) -> Unit
    }
}