package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.Constants

class PlanLineGroup {

    var key = Constants.String.BLANK
    var planLineList = emptyList<PlanLine>()
        get() {
            if (field.isEmpty()) {
                error("planLineList can not be empty")
            }
            return field
        }

    fun findSuitablePlanLine(): PlanLine? {
        var suitablePlanLine: PlanLine? = null
        var minimumPlanCostTime = 0L
        var minimumLastPlanEndTime = 0L
        for ((index, planLine) in this.planLineList.withIndex()) {
            val totalPlanCostTime = planLine.getTotalPlanCostTime()
            val lastPlanEndTime = planLine.getLastPlanEndTime()
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
        this.planLineList.forEach { totalPlanCostTime += it.getTotalPlanCostTime() }
        return totalPlanCostTime
    }
}