package com.oneliang.ktx.frame.parallel

interface ParallelTransformProcessor<IN, OUT> {

    fun process(value: IN, parallelContext: ParallelContext<OUT>)
}