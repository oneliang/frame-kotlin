package com.oneliang.ktx.frame.coroutine

import kotlinx.coroutines.Job

open class AsyncQueueProcessor : AsyncProcessor(1) {
    /**
     * launch
     */
    fun launch(
        beforeExecute: suspend () -> Unit = { },
        block: suspend () -> Unit,
        afterExecute: suspend () -> Unit = { },
        exceptionCallback: suspend (e: Throwable, hashCode: Int) -> Boolean = { _, _ -> true },
    ): Job {
        val executeBlock: suspend () -> Unit = {
            beforeExecute()
            block()
            afterExecute()
        }
        return super.launch(executeBlock, exceptionCallback)
    }
}