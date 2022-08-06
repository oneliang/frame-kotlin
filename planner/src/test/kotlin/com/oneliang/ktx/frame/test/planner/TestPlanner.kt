package com.oneliang.ktx.frame.test.planner

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.planner.*
import com.oneliang.ktx.util.common.getDayZeroTime
import java.util.*

fun main() {
    val LINE_A = "A"
    val LINE_B = "B"
    val LINE_C = "C"
    val planLineGroupA = PlanLineGroup().also {
        it.key = LINE_A
        it.planLineList = listOf(PlanLine(LINE_A + "_1").apply {
            this.planTimeList = listOf(PlanTime(0, 96 * Constants.Time.MILLISECONDS_OF_HOUR))
        }, PlanLine(LINE_A + "_2").apply {
            this.planTimeList = listOf(PlanTime(0, 96 * Constants.Time.MILLISECONDS_OF_HOUR))
        })
    }
    val planLineGroupB = PlanLineGroup().also {
        it.key = LINE_B
        it.planLineList = listOf(PlanLine(LINE_B + "_1").apply {
            this.planTimeList = listOf(PlanTime(0, 96 * Constants.Time.MILLISECONDS_OF_HOUR))
        }, PlanLine(LINE_B + "_2").apply {
            this.planTimeList = listOf(PlanTime(0, 96 * Constants.Time.MILLISECONDS_OF_HOUR))
        })
    }
    val planLineGroupC = PlanLineGroup().also {
        it.key = LINE_C
        it.planLineList = listOf(PlanLine(LINE_C + "_1").apply {
            this.planTimeList = listOf(PlanTime(0, 96 * Constants.Time.MILLISECONDS_OF_HOUR))
        }, PlanLine(LINE_C + "_2").apply {
            this.planTimeList = listOf(PlanTime(0, 96 * Constants.Time.MILLISECONDS_OF_HOUR))
        })
    }
    val planTaskABC = PlanTask().also {
        it.key = "ABC"
        it.stepList = listOf(PlanTask.Step(LINE_A).apply {
            this.planCostTime = 1 * Constants.Time.MILLISECONDS_OF_HOUR
        }, PlanTask.Step(LINE_B).apply {
            this.planCostTime = 2 * Constants.Time.MILLISECONDS_OF_HOUR
        }, PlanTask.Step(LINE_C).apply {
            this.planCostTime = 3 * Constants.Time.MILLISECONDS_OF_HOUR
        })
    }
    val planTaskA = PlanTask().also {
        it.key = "A"
        it.stepList = listOf(PlanTask.Step(LINE_A).apply {
            this.planCostTime = 1 * Constants.Time.MILLISECONDS_OF_HOUR
        })
    }
    val planTaskB = PlanTask().also {
        it.key = "B"
        it.stepList = listOf(PlanTask.Step(LINE_B).apply {
            this.planCostTime = 2 * Constants.Time.MILLISECONDS_OF_HOUR
        })
    }
    val planTaskC = PlanTask().also {
        it.key = "C"
        it.stepList = listOf(PlanTask.Step(LINE_C).apply {
            this.planCostTime = 3 * Constants.Time.MILLISECONDS_OF_HOUR
        })
    }
    val planTaskA1 = PlanTask().also {
        it.key = "A1"
        it.stepList = listOf(PlanTask.Step(LINE_A).apply {
            this.planCostTime = 1 * Constants.Time.MILLISECONDS_OF_HOUR
        })
    }
    val planTaskB1 = PlanTask().also {
        it.key = "B1"
        it.stepList = listOf(PlanTask.Step(LINE_B).apply {
            this.planCostTime = 2 * Constants.Time.MILLISECONDS_OF_HOUR
        })
    }
    val planTaskC1 = PlanTask().also {
        it.key = "C1"
        it.stepList = listOf(PlanTask.Step(LINE_C).apply {
            this.planCostTime = 3 * Constants.Time.MILLISECONDS_OF_HOUR
        })
    }
    val planLineGroupList = listOf(planLineGroupA, planLineGroupB, planLineGroupC)
    val planTaskList = listOf(planTaskABC, planTaskA, planTaskB, planTaskC, planTaskA1, planTaskB1, planTaskC1)
    Planner.plan(planLineGroupList, planTaskList)
    Planner.print(planLineGroupList, Date().getDayZeroTime())
}