package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.Constants

class PlanTask(var key: String = Constants.String.BLANK, var stepList: List<Step> = emptyList()) {

    var planTotalCostTime = 0L
    var realCostTime = 0L

    fun isSingleStepTask(): Boolean {
        return this.stepList.size == 1
    }

    class Step(val planLineGroupKey: String = Constants.String.BLANK, var planCostTime: Long = 0L)

    fun getTotalPlanCostTime(): Long {
        var totalPlanCostTime = 0L
        this.stepList.forEach {
            totalPlanCostTime += it.planCostTime
        }
        return totalPlanCostTime
    }
}