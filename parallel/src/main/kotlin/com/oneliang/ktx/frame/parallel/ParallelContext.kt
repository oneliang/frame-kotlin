package com.oneliang.ktx.frame.parallel

interface ParallelContext<IN : Any?> {

    val parentParallelContextAction: ParallelContextAction

    fun collect(value: IN)
}