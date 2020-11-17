package com.oneliang.ktx.frame.collector

import com.oneliang.ktx.util.concurrent.ThreadPool

abstract class CollectJob {

    abstract fun collect(threadPool: ThreadPool)

    abstract fun finishCallback()
}