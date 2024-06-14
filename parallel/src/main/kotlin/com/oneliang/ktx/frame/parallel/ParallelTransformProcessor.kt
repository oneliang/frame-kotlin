package com.oneliang.ktx.frame.parallel

interface ParallelTransformProcessor<IN : Any?, OUT : Any?> {

    fun process(value: IN, parallelTransformContext: ParallelTransformContext<OUT>)
}