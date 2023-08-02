package com.oneliang.ktx.frame.collector

import com.oneliang.ktx.util.concurrent.ThreadPool

abstract class CollectJob {

    /**
     * collect with thread pool
     * @param threadPool
     */
    abstract fun collect(threadPool: ThreadPool)

    /**
     * finished callback, callback when finished, included exception
     */
    abstract fun finishedCallback()
}