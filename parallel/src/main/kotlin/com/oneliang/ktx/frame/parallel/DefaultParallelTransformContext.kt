package com.oneliang.ktx.frame.parallel

import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.ConcurrentHashMap

internal class DefaultParallelTransformContext(
    private val coroutine: Coroutine,
    private val parallelJobStep: ParallelJobStep<Any?>,
    override val parallelContextAction: ParallelContextAction = ParallelContextAction.NONE,
    private val parallelJob: ParallelJob<Any?>
) : ParallelTransformContext<Any?> {
    companion object {
        private val logger = LoggerManager.getLogger(DefaultParallelTransformContext::class)
    }

    private val parallelTransformContextMap = ConcurrentHashMap<Pair<ParallelJobStep<Any?>, ParallelContextAction>, ParallelTransformContext<Any?>>()

    override fun collect(value: Any?) {
        logger.debug("transform processor, value:%s", value)
        var nextParallelJobStep: ParallelJobStep<Any?>? = null
        if (this.parallelJobStep.hasNextParallelJobStep()) {
            nextParallelJobStep = this.parallelJobStep.nextParallelJobStep
        }
        if (nextParallelJobStep == null) {
            logger.error(
                "this parallel job step is used for a transform processor, but next parallel job step is null, you may be need to add a transform processor or a sink processor for next job, this:%s",
                this.parallelJobStep
            )
        } else {
            val fixNextParallelJobStep = nextParallelJobStep
            var parallelTransformContext: ParallelTransformContext<Any?>? = null
            if (fixNextParallelJobStep.isParallelTransformProcessor()) {
                val parallelTransformContextKey = fixNextParallelJobStep to parallelContextAction
                parallelTransformContext = parallelTransformContextMap.getOrPut(parallelTransformContextKey) {
                    DefaultParallelTransformContext(this.coroutine, fixNextParallelJobStep, parallelContextAction, this.parallelJob)
                }
            }

            ParallelContextUtil.collectForParallelProcessor(this.coroutine, this.parallelJob, fixNextParallelJobStep, value, parallelContextAction, parallelTransformContext)

        }
    }
}