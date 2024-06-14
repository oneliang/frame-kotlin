package com.oneliang.ktx.frame.parallel

interface ParallelTransformContext<IN : Any?> {

    val parentParallelContextAction: ParallelContextAction

    fun collect(value: IN)
}