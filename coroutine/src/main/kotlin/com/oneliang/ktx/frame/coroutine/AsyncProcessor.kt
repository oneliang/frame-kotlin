package com.oneliang.ktx.frame.coroutine

import com.oneliang.ktx.frame.coroutine.Coroutine
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

open class AsyncProcessor(maxThreads: Int = Runtime.getRuntime().availableProcessors()) {

    private val executorService = Executors.newFixedThreadPool(maxThreads)
    private val coroutine = Coroutine(executorService.asCoroutineDispatcher())
    private val taskMap = ConcurrentHashMap<Int, Boolean>()

    @Volatile
    private var needToAutoDestroy = false

    /**
     * launch
     */
    fun launch(
        block: suspend () -> Unit,
        exceptionCallback: suspend (e: Throwable, hashCode: Int) -> Boolean = { _, _ -> true },
    ): Job {

        val hashCode = block.hashCode()
        this.taskMap[hashCode] = false
        return this.coroutine.launch {
            try {
                block()
            } catch (e: Throwable) {
                val throwExceptionSign = exceptionCallback(e, hashCode)
                if (!throwExceptionSign) {
                    throw e
                }
            } finally {
                this.taskMap[hashCode] = true
            }
            checkNeedToAutoDestroy()
        }
    }

    /**
     * check need to auto destroy after each task which is finished
     */
    protected fun checkNeedToAutoDestroy() {
        if (!this.needToAutoDestroy) {
            return
        }
        var result = true
        run loop@{
            this.taskMap.forEach { (key, value) ->
                if (!value) {
                    result = false
                    return@loop//break
                }
            }
        }
        if (result) {
            this.executorService.shutdown()
            this.taskMap.clear()
            println("checkNeedToAutoDestroy: ")
        }
    }

    /**
     * auto destroy
     */
    fun autoDestroy() {
        this.needToAutoDestroy = true
    }
}