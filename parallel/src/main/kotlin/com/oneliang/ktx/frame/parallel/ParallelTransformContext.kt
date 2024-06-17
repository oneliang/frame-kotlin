package com.oneliang.ktx.frame.parallel

interface ParallelTransformContext<IN : Any?> {

    val parallelContextAction: ParallelContextAction

    fun collect(value: IN)
}