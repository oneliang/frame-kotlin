package com.oneliang.ktx.frame.parallel

interface ParallelSourceContext<IN> {

    fun collect(value: IN, parallelContextAction: ParallelContextAction = ParallelContextAction.NONE)
}