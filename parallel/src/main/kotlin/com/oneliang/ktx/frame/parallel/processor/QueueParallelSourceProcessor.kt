package com.oneliang.ktx.frame.parallel.processor

import com.oneliang.ktx.frame.parallel.ParallelContextAction
import com.oneliang.ktx.frame.parallel.ParallelSourceContext
import com.oneliang.ktx.frame.parallel.ParallelSourceProcessor
import java.util.concurrent.ConcurrentLinkedQueue

open class QueueParallelSourceProcessor<T : Any> : ParallelSourceProcessor<T> {

    private val lock = Object()//maybe a small lock in java
    private val queue = ConcurrentLinkedQueue<Pair<T, ParallelContextAction>>()

    override suspend fun process(parallelSourceContext: ParallelSourceContext<T>) {
        while (true) {
            if (this.queue.isNotEmpty()) {
                val (resource, parallelContextAction) = this.queue.poll()
                parallelSourceContext.collect(resource, parallelContextAction)
            } else {
                synchronized(this.lock) {
                    this.lock.wait()
                }
            }
        }
    }

    fun addResource(resource: T, parallelContextAction: ParallelContextAction = ParallelContextAction.NONE) {
        this.queue.offer(resource to parallelContextAction)
        synchronized(this.lock) {
            this.lock.notifyAll()
        }
    }
}