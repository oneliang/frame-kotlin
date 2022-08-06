package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.util.common.toFormatString
import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.common.toUtilDate
import com.oneliang.ktx.util.logging.LoggerManager

object Planner {
    private val logger = LoggerManager.getLogger(Planner::class)

    /**
     * @param planLineGroupList
     * @param planTaskList sorted by outside will effect the result
     */
    fun plan(planLineGroupList: List<PlanLineGroup>, planTaskList: List<PlanTask>) {
        //1.find the single step task
        val planLineGroupMap = planLineGroupList.toMap { it.key to it }
        val planLineGroupAllPlanStepCostMap = mutableMapOf<String, Long>()
//        val singleStepPlanTaskList = mutableListOf<PlanTask>()
//        val multiStepPlanTaskList = mutableListOf<PlanTask>()
        val sortedPlanTaskList = planTaskList//.sortedByDescending { it.getTotalPlanCostTime() }
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
                    beginTime = previousPlanLine.getLastIdleTime(planTaskStep.planCostTime)
                }
                logger.info("plan line name:%s, add plan task step, task key:%s, plan line group key:%s, begin time:%s", planLine.name, planTask.key, planTaskStep.planLineGroupKey, beginTime)
                planLine.addPlanTaskStep(planTask, planTaskStep, beginTime)
                previousPlanLine = planLine
            }
        }
    }

    fun print(planLineGroupList: List<PlanLineGroup>, beginTime: Long, printPlanStep: Boolean = false) {
        planLineGroupList.forEach { planLineGroup ->
            logger.info("group key:%s", planLineGroup.key)
            planLineGroup.planLineList.forEach { planLine ->
                if (printPlanStep) {
                    planLine.planStepList.forEach { planStep ->
                        val planTask = planStep.planTask
                        val planTaskStep = planStep.planTaskStep
                        val planBeginDateString = (beginTime + planStep.planBeginTime).toUtilDate().toFormatString()
                        val planEndDateString = (beginTime + planStep.planEndTime).toUtilDate().toFormatString()
                        logger.info("plan task key:%s, plan task step, plan line group key:%s, begin time:%s, end time:%s, cost time:%s", planTask.key, planTaskStep.planLineGroupKey, planBeginDateString, planEndDateString, planTaskStep.planCostTime)
                    }
                }
                val lastPlanStepEndTimeDateString = (beginTime + planLine.getLastPlanStepEndTime()).toUtilDate().toFormatString()
                logger.info("plan line name:%s, total plan cost time:%s, last plan step end time:%s", planLine.name, planLine.getTotalPlanStepCostTime(), lastPlanStepEndTimeDateString)
            }
        }
    }
}