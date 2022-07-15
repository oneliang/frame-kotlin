package com.oneliang.ktx.frame.parallel

interface ParallelContext<IN> {

    val parentParallelContextAction: ParallelContextAction

    fun collect(value: IN)
}