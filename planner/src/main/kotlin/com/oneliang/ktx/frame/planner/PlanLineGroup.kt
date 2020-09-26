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
        var minimumLastIdleTime = 0L
        for (planLine in this.planLineList) {
            val lastIdleTime = planLine.getLastIdleTime()
            if (minimumLastIdleTime == 0L || minimumLastIdleTime < lastIdleTime) {
                minimumLastIdleTime = lastIdleTime
                suitablePlanLine = planLine
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