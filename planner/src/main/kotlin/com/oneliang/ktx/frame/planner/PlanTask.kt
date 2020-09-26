package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.Constants

class PlanTask {

    var key = Constants.String.BLANK
    var planTotalCostTime = 0L
    var realCostTime = 0L
    var stepList = emptyList<Step>()

    fun isSingleStepTask(): Boolean {
        return this.stepList.size == 1
    }

    class Step(val planLineGroupKey: String = Constants.String.BLANK) {
        var planCostTime = 0L
    }

    fun getTotalPlanCostTime(): Long {
        var totalPlanCostTime = 0L
        this.stepList.forEach {
            totalPlanCostTime += it.planCostTime
        }
        return totalPlanCostTime
    }
}