package com.oneliang.ktx.frame.parallel

interface ParallelSourceContext<IN : Any?> {

    fun collect(value: IN, parallelContextAction: ParallelContextAction = ParallelContextAction.NONE)
}