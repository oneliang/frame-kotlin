package com.oneliang.ktx.frame.coroutine

import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Coroutine(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error("Throws an exception with message: %s", throwable, throwable.message)
    }
) {
    companion object {
        private val logger = LoggerManager.getLogger(Coroutine::class)
    }

    /**
     * coroutine scope
     */
    private val coroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = coroutineContext
    }

    /**
     * run blocking
     * @param block
     */
    fun runBlocking(block: suspend () -> Unit) {
        runBlocking(this.coroutineScope.coroutineContext + this.coroutineExceptionHandler) {
            block()
        }
    }

    /**
     * launch
     * @param block
     * @return Job
     */
    fun launch(block: suspend () -> Unit): Job {
        return this.coroutineScope.launch(this.coroutineScope.coroutineContext + this.coroutineExceptionHandler) {
            block()
        }
    }

    /**
     * sync
     * @param block
     */
    fun sync(block: suspend Coroutine.() -> Unit) {
        runBlocking {
            block(this)
        }
    }

    /**
     * async
     * @param block
     */
    fun async(block: suspend Coroutine.() -> Unit): Job {
        return launch {
            block(this)
        }
    }
}