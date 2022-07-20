package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.frame.parallel.cache.CacheData
import com.oneliang.ktx.util.logging.LoggerManager

internal object ParallelContextUtil {
    private val logger = LoggerManager.getLogger(ParallelContextUtil::class)
    internal fun collectForParallelProcessor(coroutine: Coroutine, parallelJob: ParallelJob<Any?>, parallelJobStep: ParallelJobStep<Any?>, value: Any?, parentParallelContextAction: ParallelContextAction) {
        when {
            parallelJobStep.isParallelTransformProcessor() -> {
                logger.debug("transform processor, value:%s", value)
                var nextParallelJobStep: ParallelJobStep<Any?>? = null
                if (parallelJobStep.hasNextParallelJobStep()) {
                    nextParallelJobStep = parallelJobStep.nextParallelJobStep
                }
                if (nextParallelJobStep == null) {
                    logger.error("this parallel job step is used for a transform processor, but next parallel job step is null, you may be need to add a transform processor or a sink processor for next job, this:%s", parallelJobStep)
                } else {
                    val nextParallelContext = DefaultParallelContext(coroutine, nextParallelJobStep, parentParallelContextAction, parallelJob)
                    parallelJobStep.parallelTransformProcessor.process(value, nextParallelContext)
                }
            }
            parallelJobStep.isParallelSinkProcessor() -> {
                logger.debug("sink processor, value:%s, parent context action:%s", value, parentParallelContextAction)
                for (parallelSinkProcessor in parallelJobStep.parallelSinkProcessorList) {
                    if (parallelJob.parallelJobConfiguration.async) {
                        coroutine.launch {
                            parallelSinkProcessor.sink(value)
                        }
                    } else {
                        parallelSinkProcessor.sink(value)
                    }
                    if ((parentParallelContextAction == ParallelContextAction.SAVEPOINT
                                || parentParallelContextAction == ParallelContextAction.FINISHED)
                        && parallelJob.parallelJobConfiguration.useCache
                    ) {
                        val sinkKey = parallelSinkProcessor.cacheKey
                        val sinkData = parallelJob.getSinkData(sinkKey) ?: CacheData.Data()
                        parallelSinkProcessor.savepoint(sinkData)
                        parallelJob.updateSinkData(sinkKey, sinkData)
                    }
                }
                if (parentParallelContextAction == ParallelContextAction.SAVEPOINT) {
                    parallelJob.saveCache()
                } else if (parentParallelContextAction == ParallelContextAction.FINISHED) {
                    parallelJob.saveCache()
                    parallelJob.finish()
                }
            }
            else -> {
                logger.error("unknown parallel job step")
            }
        }
    }
}