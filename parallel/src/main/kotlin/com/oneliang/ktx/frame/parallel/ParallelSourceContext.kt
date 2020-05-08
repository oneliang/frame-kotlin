package com.oneliang.ktx.frame.parallel

interface ParallelSourceContext<IN> {

    suspend fun collect(value: IN, parallelContextAction: ParallelContextAction = ParallelContextAction.NONE)
}