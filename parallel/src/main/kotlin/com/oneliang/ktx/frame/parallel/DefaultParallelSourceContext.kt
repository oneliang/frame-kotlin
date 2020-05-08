package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.frame.parallel.cache.CacheData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class DefaultParallelSourceContext(
        private val coroutineScope: CoroutineScope,
        private val parallelSourceProcessor: ParallelSourceProcessor<Any>,
        private val parallelJobStepList: List<ParallelJobStep<Any>>,
        private val parallelJob: ParallelJob<Any>) : ParallelSourceContext<Any> {
    companion object {
        private val logger = LoggerManager.getLogger(DefaultParallelSourceContext::class)
    }

    override suspend fun collect(value: Any, parallelContextAction: ParallelContextAction) {
        this.parallelJobStepList.forEach {
            if (this.parallelJob.parallelJobConfiguration.async) {
                this.coroutineScope.launch(this.coroutineScope.coroutineContext) {
                    ParallelContextUtil.collectForParallelProcessor(coroutineScope, parallelJob, it, value, parallelContextAction)
                }
            } else {
                if ((parallelContextAction == ParallelContextAction.SAVEPOINT
                                || parallelContextAction == ParallelContextAction.FINISHED)
                        && this.parallelJob.parallelJobConfiguration.useCache) {
                    val sourceKey = this.parallelSourceProcessor.cacheKey
                    val sourceData = this.parallelJob.getSourceData(sourceKey) ?: CacheData.Data()
                    this.parallelSourceProcessor.savepoint(sourceData)
                    this.parallelJob.updateSourceData(sourceKey, sourceData)
                }
                ParallelContextUtil.collectForParallelProcessor(coroutineScope, parallelJob, it, value, parallelContextAction)
            }
        }
    }
}