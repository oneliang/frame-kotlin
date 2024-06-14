package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.logging.LoggerManager

internal class DefaultParallelTransformContext(
        private val coroutine: Coroutine,
        private val parallelJobStep: ParallelJobStep<Any?>,
        override val parentParallelContextAction: ParallelContextAction = ParallelContextAction.NONE,
        private val parallelJob: ParallelJob<Any?>) : ParallelTransformContext<Any?> {
    companion object {
        private val logger = LoggerManager.getLogger(DefaultParallelTransformContext::class)
    }

    override fun collect(value: Any?) {
        if (this.parallelJob.parallelJobConfiguration.async) {
            this.coroutine.launch {
                ParallelContextUtil.collectForParallelProcessor(this.coroutine, this.parallelJob, this.parallelJobStep, value, this.parentParallelContextAction)
            }
        } else {
            ParallelContextUtil.collectForParallelProcessor(this.coroutine, this.parallelJob, this.parallelJobStep, value, this.parentParallelContextAction)
        }
    }

}