package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.logging.LoggerManager

class Planner {
    companion object {
        private val logger = LoggerManager.getLogger(Planner::class)
    }

    var planLineGroupList = emptyList<PlanLineGroup>()
    var planTaskList = emptyList<PlanTask>()

    fun plan() {
        //1.find the single step task
        val planLineGroupMap = this.planLineGroupList.toMap { it.key to it }
        val planLineGroupAllPlanStepCostMap = mutableMapOf<String, Long>()
//        val singleStepPlanTaskList = mutableListOf<PlanTask>()
//        val multiStepPlanTaskList = mutableListOf<PlanTask>()
        val sortedPlanTaskList = this.planTaskList.sortedByDescending { it.getTotalPlanCostTime() }
        sortedPlanTaskList.forEach { planTask ->
            //            if (planTask.isSingleStepTask()) {
//                singleStepPlanTaskList += planTask
//            } else {
//                multiStepPlanTaskList += planTask
//            }
            for (step in planTask.stepList) {
                val costTime = planLineGroupAllPlanStepCostMap.getOrPut(step.planLineGroupKey) { 0L }
                planLineGroupAllPlanStepCostMap[step.planLineGroupKey] = costTime + step.planCostTime
            }
        }
//        singleStepPlanTaskList.forEach { planTask ->
//            planTask.stepList.forEach { planTaskStep ->
//                val groupKey = planTaskStep.planLineGroupKey
//                val planLineGroup = planLineGroupMap[groupKey] ?: error("$groupKey not exists")
//                val planLine = planLineGroup.findSuitablePlanLine() ?: error("$groupKey no suitable plan line")
//                planLine.addPlanTaskStep(planTask, planTaskStep)
//            }
//        }
        sortedPlanTaskList.forEach { planTask ->
            var previousPlanLine: PlanLine? = null
            for (planTaskStep in planTask.stepList) {//step list will execute in order
                val groupKey = planTaskStep.planLineGroupKey
                val planLineGroup = planLineGroupMap[groupKey] ?: error("$groupKey not exists")
                val planLine = planLineGroup.findSuitablePlanLine() ?: error("$groupKey no suitable plan line")
                var beginTime = 0L
                if (previousPlanLine != null) {
                    beginTime = previousPlanLine.getLastIdleTime()
                }
                logger.info("add plan task step, task key:%s, plan line group key:%s, begin time:%s", planTask.key, planTaskStep.planLineGroupKey, beginTime)
                planLine.addPlanTaskStep(planTask, planTaskStep, beginTime)
                previousPlanLine = planLine
            }
        }
    }

    fun print() {
        this.planLineGroupList.forEach { planLineGroup ->
            logger.info("group key:%s", planLineGroup.key)
            planLineGroup.planLineList.forEach { planLine ->
                planLine.planStepList.forEach { planStep ->
                    val planTask = planStep.planTask
                    val planTaskStep = planStep.planTaskStep
                    logger.info("plan task key:%s, plan task step, plan line group key:%s, plan cost time:%s, begin time:%s, end time:%s, cost time:%s", planTask.key, planTaskStep.planLineGroupKey, planTaskStep.planCostTime, planStep.planBeginTime, planStep.planEndTime, planTaskStep.planCostTime)
                }
            }
        }
    }
}