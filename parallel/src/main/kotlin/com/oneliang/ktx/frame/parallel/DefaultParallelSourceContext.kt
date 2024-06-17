package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.frame.parallel.cache.CacheData
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap

internal class DefaultParallelSourceContext(
    private val coroutine: Coroutine,
    private val parallelSourceProcessor: ParallelSourceProcessor<Any?>,
    private val parallelJobStepList: List<ParallelJobStep<Any?>>,
    private val parallelJob: ParallelJob<Any?>
) : ParallelSourceContext<Any?> {
    companion object {
        private val logger = LoggerManager.getLogger(DefaultParallelSourceContext::class)
    }

    private val parallelTransformContextMap = ConcurrentHashMap<Pair<ParallelJobStep<Any?>, ParallelContextAction>, ParallelTransformContext<Any?>>()

    override fun collect(value: Any?, parallelContextAction: ParallelContextAction) {
        if (this.parallelJobStepList.isEmpty()) {
            this.parallelJob.finish()
        }
        this.parallelJobStepList.forEach {
            var parallelTransformContext: ParallelTransformContext<Any?>? = null
            if (it.isParallelTransformProcessor()) {
                val parallelTransformContextKey = it to parallelContextAction
                parallelTransformContext = this.parallelTransformContextMap.getOrPut(parallelTransformContextKey) {
                    DefaultParallelTransformContext(this.coroutine, it, parallelContextAction, this.parallelJob)
                }
            }

            ParallelContextUtil.collectForParallelProcessor(this.coroutine, this.parallelJob, it, value, parallelContextAction, parallelTransformContext)

        }

        // for parallel source  processor
        if (!this.parallelJob.parallelJobConfiguration.async) {
            if ((parallelContextAction == ParallelContextAction.SAVEPOINT
                        || parallelContextAction == ParallelContextAction.FINISHED)
                && this.parallelJob.parallelJobConfiguration.useCache
            ) {
                val sourceKey = this.parallelSourceProcessor.cacheKey
                val sourceData = this.parallelJob.getSourceData(sourceKey) ?: CacheData.Data()
                this.parallelSourceProcessor.savepoint(sourceData)
                this.parallelJob.updateSourceData(sourceKey, sourceData)
            }
        }
    }
}