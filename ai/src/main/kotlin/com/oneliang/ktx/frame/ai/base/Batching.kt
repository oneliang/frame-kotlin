package com.oneliang.ktx.frame.ai.base

abstract class Batching<OUT : Any>(open val batchSize: Int) {

    abstract fun reset()

    abstract fun fetch(): Result<OUT>

    class Result<DATA : Any>(var finished: Boolean = false, var dataList: List<DATA> = emptyList())
}