package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.Constants

class PlanLineGroup(var key: String = Constants.String.BLANK, var planLineList: List<PlanLine> = emptyList()) {

    fun findSuitablePlanLine(): PlanLine? {
        var suitablePlanLine: PlanLine? = null
        var minimumPlanCostTime = 0L
        var minimumLastPlanEndTime = 0L
        //find plan line which last plan end time is minimum
        for ((index, planLine) in this.planLineList.withIndex()) {
            val totalPlanCostTime = planLine.getTotalPlanStepCostTime()
            val lastPlanEndTime = planLine.getLastPlanStepEndTime()
            if (index == 0) {
                minimumPlanCostTime = totalPlanCostTime
                minimumLastPlanEndTime = lastPlanEndTime
                suitablePlanLine = planLine
            } else if (lastPlanEndTime < minimumLastPlanEndTime) {
                minimumLastPlanEndTime = lastPlanEndTime
                suitablePlanLine = planLine
//            } else if (totalPlanCostTime < minimumPlanCostTime) {
//                minimumPlanCostTime = totalPlanCostTime
//                suitablePlanLine = planLine
            }
        }
        return suitablePlanLine
    }

    fun getTotalPlanCostTime(): Long {
        var totalPlanCostTime = 0L
        this.planLineList.forEach { totalPlanCostTime += it.getTotalPlanStepCostTime() }
        return totalPlanCostTime
    }
}