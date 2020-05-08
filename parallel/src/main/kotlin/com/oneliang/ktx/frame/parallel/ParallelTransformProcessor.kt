package com.oneliang.ktx.frame.parallel

interface ParallelTransformProcessor<IN, OUT> {

    suspend fun process(value: IN, parallelContext: ParallelContext<OUT>)
}