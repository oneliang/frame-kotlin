package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.util.logging.LoggerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class DefaultParallelContext(
        private val coroutineScope: CoroutineScope,
        private val parallelJobStep: ParallelJobStep<Any>,
        override val parentParallelContextAction: ParallelContextAction = ParallelContextAction.NONE,
        private val parallelJob: ParallelJob<Any>) : ParallelContext<Any> {
    companion object {
        private val logger = LoggerManager.getLogger(DefaultParallelContext::class)
    }

    override suspend fun collect(value: Any) {
        if (this.parallelJob.parallelJobConfiguration.async) {
            this.coroutineScope.launch(this.coroutineScope.coroutineContext) {
                ParallelContextUtil.collectForParallelProcessor(coroutineScope, parallelJob, parallelJobStep, value, parentParallelContextAction)
            }
        } else {
            ParallelContextUtil.collectForParallelProcessor(coroutineScope, parallelJob, parallelJobStep, value, parentParallelContextAction)
        }
    }

}