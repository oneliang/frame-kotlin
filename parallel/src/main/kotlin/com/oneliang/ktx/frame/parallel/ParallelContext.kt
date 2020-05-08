package com.oneliang.ktx.frame.parallel

interface ParallelContext<IN> {

    val parentParallelContextAction: ParallelContextAction

    suspend fun collect(value: IN)
}