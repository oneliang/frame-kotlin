package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.concurrent.ResourceQueueThread
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class TestConcurrent : ResourceQueueThread.ResourceProcessor<Int> {

    var total = 100000
    override fun process(resource: Int) {
        total--
    }

    val queue = ResourceQueueThread(this)
}

fun main() {
    main1()
    main2()
}

//
fun main1() {
    val testConcurrent = TestConcurrent()
    testConcurrent.queue.start()
    val executorService = Executors.newFixedThreadPool(4)
    val coroutine = Coroutine(executorService.asCoroutineDispatcher())
    val begin = System.currentTimeMillis()
    coroutine.runBlocking {
        val jobList = mutableListOf<Job>()
        repeat(100000) {
            jobList += coroutine.launch {
                testConcurrent.queue.addResource(0)
            }
        }
        jobList.forEach {
            it.join()
        }
    }
    println("result:%s, cost:%s".format(testConcurrent.total, System.currentTimeMillis() - begin))
    Thread.sleep(1000)
    testConcurrent.queue.stop()
    executorService.shutdown()
}

fun main2() {
    val atomicInteger = AtomicInteger(100000)
    val executorService = Executors.newFixedThreadPool(4)
    val coroutine = Coroutine(executorService.asCoroutineDispatcher())
    val begin = System.currentTimeMillis()
    coroutine.runBlocking {
        val jobList = mutableListOf<Job>()
        repeat(100000) {
            jobList += coroutine.launch {
                atomicInteger.decrementAndGet()
            }
        }
        jobList.forEach {
            it.join()
        }
    }
    println("result:%s, cost:%s".format(atomicInteger.get(), System.currentTimeMillis() - begin))
    executorService.shutdown()
}