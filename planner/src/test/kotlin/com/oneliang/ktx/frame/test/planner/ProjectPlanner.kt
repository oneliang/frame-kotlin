package com.oneliang.ktx.frame.test.planner

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.planner.*
import com.oneliang.ktx.util.common.getDayZeroTime
import java.util.*

const val GROUP_FRONT = "front"
const val GROUP_BACKEND = "backend"
const val GROUP_FRONT_LINE_A = "${GROUP_FRONT}_A"
const val GROUP_FRONT_LINE_B = "${GROUP_FRONT}_B"
const val GROUP_BACKEND_LINE_A = "${GROUP_BACKEND}_A"
const val GROUP_BACKEND_LINE_B = "${GROUP_BACKEND}_B"
private fun generatePlanTimeList(days: Int): List<PlanTime> {
    val planTimeList = mutableListOf<PlanTime>()
    for (day in 0 until days) {
        planTimeList += PlanTime((day * 24 + 9) * Constants.Time.MILLISECONDS_OF_HOUR, (day * 24 + 18) * Constants.Time.MILLISECONDS_OF_HOUR)
    }
    return planTimeList
}

val planTimeList = generatePlanTimeList(10)//listOf(PlanTime(9 * Constants.Time.MILLISECONDS_OF_HOUR, 18 * Constants.Time.MILLISECONDS_OF_HOUR))
fun main() {
    val frontPlanLineGroup = PlanLineGroup(GROUP_FRONT)
    val backendPlanLineGroup = PlanLineGroup(GROUP_BACKEND)
    frontPlanLineGroup.planLineList = listOf(
        PlanLine(GROUP_FRONT_LINE_A).also {
            it.planTimeList = planTimeList
        }, PlanLine(GROUP_FRONT_LINE_B).also {
            it.planTimeList = planTimeList
        })
    backendPlanLineGroup.planLineList = listOf(
        PlanLine(GROUP_BACKEND_LINE_A).also {
            it.planTimeList = planTimeList
        }, PlanLine(GROUP_BACKEND_LINE_B).also {
            it.planTimeList = planTimeList
        })
    val planTask1 = PlanTask("浏览埋点").also {
        it.stepList = listOf(PlanTask.Step(GROUP_FRONT, 10 * Constants.Time.MILLISECONDS_OF_HOUR))
    }
    val planTask2 = PlanTask("点击埋点").also {
        it.stepList = listOf(PlanTask.Step(GROUP_FRONT, 10 * Constants.Time.MILLISECONDS_OF_HOUR))
    }
    val planTask3 = PlanTask("产业招商").also {
        it.stepList = listOf(
            PlanTask.Step(GROUP_FRONT, 22 * Constants.Time.MILLISECONDS_OF_HOUR),
        )
    }
    val planTask4 = PlanTask("产业招商").also {
        it.stepList = listOf(
            PlanTask.Step(GROUP_BACKEND, 12 * Constants.Time.MILLISECONDS_OF_HOUR)
        )
    }

    val planTaskList = listOf(planTask1, planTask2, planTask3, planTask4)
    Planner.plan(listOf(frontPlanLineGroup, backendPlanLineGroup), planTaskList)
    Planner.print(listOf(frontPlanLineGroup, backendPlanLineGroup), Date().getDayZeroTime())
}