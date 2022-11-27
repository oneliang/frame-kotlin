package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.util.common.sumByLong
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.math.segment.Segmenter

class PlanLine(val name: String, val beginTime: Long) {
    companion object {
        private val logger = LoggerManager.getLogger(PlanLine::class)
    }

    var executableTime = 0L

    //    var planCostTime = 0L
    var executeTime = 0L
    var currentPlanTask: PlanTask? = null

    private val privatePlanItemList = mutableListOf<PlanItem>()
    val planItemList: List<PlanItem>
        get() {
            return this.privatePlanItemList
        }
    private lateinit var planTimeSegmentList: List<Segmenter.Segment<Any?>>
    private lateinit var splitSegmentList: List<Segmenter.Segment<Any?>>
    var planTimeList = emptyList<PlanTime>()
        set(value) {
            field = value
            this.planTimeSegmentList = value.toSegmentList()
            this.splitSegmentList = this.planTimeSegmentList
        }
        get() {
            if (field.isEmpty()) {
                error("planTimeList can not be empty")
            }
            return field
        }

    fun addPlanTaskStep(planTask: PlanTask, planTaskStep: PlanTask.Step, newBeginTime: Long = 0) {
        val lastPlanStep = this.planItemList.lastOrNull()
        val planCostTime = planTaskStep.planCostTime
        var beginTime = 0L
        if (newBeginTime > 0) {
            beginTime = newBeginTime
        } else {
            if (lastPlanStep != null) {
                beginTime = lastPlanStep.planEndTime
            }
        }
        this.splitSegmentList = Segmenter.resetAndSplitSegment(this.splitSegmentList, beginTime, planCostTime to (planTask to planTaskStep))
        this.privatePlanItemList.clear()
        this.splitSegmentList.forEach {
            val data = it.data
            if (data != null && data is Pair<*, *>) {
                val task = data.first
                val taskStep = data.second
                if (task != null && task is PlanTask && taskStep != null && taskStep is PlanTask.Step) {
                    this.privatePlanItemList += PlanItem(task, taskStep).apply {
                        this.planBeginTime = it.begin
                        this.planEndTime = it.end
                    }
                } else {
                    logger.error("plan task is null or plan task step is null, plan task:%s, plan task step:%s", task, taskStep)
                }
            }
        }
    }

    internal fun getLastIdleTime(length: Long): Long {
        return if (this.planItemList.isEmpty()) {//when plan step list is empty, get the first plan time
            this.planTimeList.first().begin
        } else {
            val (found, begin) = Segmenter.findSuitableBegin(this.planTimeSegmentList, 0L, length)
            if (found) {
                begin
            } else {
                this.planItemList.last().planEndTime
            }
        }
    }

    fun getTotalPlanStepCostTime(): Long {
        return this.planItemList.sumByLong { it.planEndTime - it.planBeginTime }
    }

    fun getLastPlanStepEndTime(): Long {
        return this.planItemList.lastOrNull()?.planEndTime ?: 0L
    }
}