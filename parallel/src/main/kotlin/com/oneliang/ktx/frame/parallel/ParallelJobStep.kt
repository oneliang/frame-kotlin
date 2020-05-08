package com.oneliang.ktx.frame.parallel

class ParallelJobStep<IN> {

    internal lateinit var parallelTransformProcessor: ParallelTransformProcessor<IN, Any>
    internal val parallelSinkProcessorList = mutableListOf<ParallelSinkProcessor<IN>>()
    internal lateinit var nextParallelJobStep: ParallelJobStep<Any>

    @Suppress("UNCHECKED_CAST")
    fun <OUT> addParallelTransformProcessor(parallelProcessor: ParallelTransformProcessor<IN, OUT>): ParallelJobStep<OUT> {
        if (this::parallelTransformProcessor.isInitialized) {
            error("parallel transform processor has been initialized, only can initialize one time")
        }
        this.parallelTransformProcessor = parallelProcessor as ParallelTransformProcessor<IN, Any>
        val parallelJobStep = ParallelJobStep<OUT>()
        this.nextParallelJobStep = parallelJobStep as ParallelJobStep<Any>
        return parallelJobStep
    }

    fun addParallelSinkProcessor(parallelSinkProcessor: ParallelSinkProcessor<IN>) {
        this.parallelSinkProcessorList += parallelSinkProcessor
    }

    internal fun isParallelTransformProcessor(): Boolean {
        return this.parallelSinkProcessorList.isEmpty() && this::nextParallelJobStep.isInitialized && this::parallelTransformProcessor.isInitialized
    }

    internal fun isParallelSinkProcessor(): Boolean {
        return this.parallelSinkProcessorList.isNotEmpty() && !this::nextParallelJobStep.isInitialized && !this::parallelTransformProcessor.isInitialized
    }

    internal fun hasNextParallelJobStep(): Boolean {
        return this::nextParallelJobStep.isInitialized
    }
}