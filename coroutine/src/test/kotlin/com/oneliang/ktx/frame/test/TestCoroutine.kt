package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.coroutine.Coroutine
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

fun main() {
    val coroutine1 = Coroutine()
    val executorService = Executors.newFixedThreadPool(4)
    val coroutineContext = executorService.asCoroutineDispatcher()
    val coroutine2 = Coroutine(coroutineContext)
    for (i in 1..10) {
        coroutine1.launch {
            delay(2000)
            println(1 * i)
        }
        coroutine2.launch {
            println(2 * i)
        }
    }
    Thread.sleep(20000)
    executorService.shutdown()
}