package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.util.logging.LoggerManager

class ParallelJobExecutor() {

    companion object {
        private val logger = LoggerManager.getLogger(ParallelJobExecutor::class)
    }

    fun <IN> execute(parallelJob: ParallelJob<IN>) {
        parallelJob.execute()
    }
}