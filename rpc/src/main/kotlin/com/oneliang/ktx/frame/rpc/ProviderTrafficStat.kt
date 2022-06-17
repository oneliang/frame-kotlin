package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.util.concurrent.ResourceQueueThread

internal class ProviderTrafficStat {

    var result: Long = 0
        private set

    private val resourceProcessor = object : ResourceQueueThread.ResourceProcessor<TrafficOperator> {
        override fun process(resource: TrafficOperator) {
            when (resource.operateType) {
                TrafficOperator.OperateType.ADD -> {
                    result += resource.value
                }
                TrafficOperator.OperateType.MINUS -> {
                    result -= resource.value
                }
            }
        }
    }
    private val resourceQueueThread = ResourceQueueThread(this.resourceProcessor)

    fun start() {
        this.resourceQueueThread.start()
    }

    fun stop() {
        this.resourceQueueThread.stop()
    }

    fun add(value: Long) {
        this.resourceQueueThread.addResource(TrafficOperator(TrafficOperator.OperateType.ADD, value))
    }

    fun minus(value: Long) {
        this.resourceQueueThread.addResource(TrafficOperator(TrafficOperator.OperateType.MINUS, value))
    }

    internal class TrafficOperator(val operateType: OperateType, var value: Long) {
        enum class OperateType {
            ADD, MINUS
        }
    }
}