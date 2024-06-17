package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.frame.parallel.cache.CacheData
import com.oneliang.ktx.util.logging.LoggerManager

internal object ParallelContextUtil {
    private val logger = LoggerManager.getLogger(ParallelContextUtil::class)
    internal fun collectForParallelProcessor(
        coroutine: Coroutine,
        parallelJob: ParallelJob<Any?>,
        parallelJobStep: ParallelJobStep<Any?>,
        value: Any?,
        parallelContextAction: ParallelContextAction,
        parallelTransformContext: ParallelTransformContext<Any?>?
    ) {
        logger.debug("parallelTransformContext:%s",parallelTransformContext)
        if (parallelJobStep.isParallelTransformProcessor() && parallelTransformContext != null) {
            if (parallelJob.parallelJobConfiguration.async) {
                coroutine.launch {
                    parallelJobStep.parallelTransformProcessor.process(value, parallelTransformContext)
                }
            } else {
                parallelJobStep.parallelTransformProcessor.process(value, parallelTransformContext)
            }
        } else if (parallelJobStep.isParallelSinkProcessor()) {
            for (parallelSinkProcessor in parallelJobStep.parallelSinkProcessorList) {
                if (parallelJob.parallelJobConfiguration.async) {
                    coroutine.launch {
                        logger.debug("sink processor, value:%s", value)
                        parallelSinkProcessor.sink(value)
                    }
                } else {
                    logger.debug("sink processor, value:%s", value)
                    parallelSinkProcessor.sink(value)
                }
                if ((parallelContextAction == ParallelContextAction.SAVEPOINT
                            || parallelContextAction == ParallelContextAction.FINISHED)
                    && parallelJob.parallelJobConfiguration.useCache
                ) {
                    val sinkKey = parallelSinkProcessor.cacheKey
                    val sinkData = parallelJob.getSinkData(sinkKey) ?: CacheData.Data()
                    parallelSinkProcessor.savepoint(sinkData)
                    parallelJob.updateSinkData(sinkKey, sinkData)
                }
            }
            if (parallelContextAction == ParallelContextAction.SAVEPOINT) {
                parallelJob.saveCache()
            } else if (parallelContextAction == ParallelContextAction.FINISHED) {
                parallelJob.saveCache()
                parallelJob.finish()
            }
        }
    }
}