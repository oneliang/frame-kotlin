package com.oneliang.ktx.frame.test.planner

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.planner.*
import com.oneliang.ktx.util.common.getDayZeroTime
import com.oneliang.ktx.util.file.readContentEachLine
import java.io.File
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

val planTimeList = generatePlanTimeList(20)//listOf(PlanTime(9 * Constants.Time.MILLISECONDS_OF_HOUR, 18 * Constants.Time.MILLISECONDS_OF_HOUR))

fun readPlanTaskFromFile(file: File): List<PlanTask> {
    val planTaskList = mutableListOf<PlanTask>()
    file.readContentEachLine { line ->
        if (line.isBlank()) {
            return@readContentEachLine true
        }
        val fixLine = line.trim()
        val dataArrays = fixLine.split(Constants.Symbol.COMMA)
        if (dataArrays.size >= 4) {
            val key = dataArrays[0] + Constants.Symbol.MINUS + Constants.Symbol.GREATER_THAN + dataArrays[1]
            val frontHour = dataArrays[2].toInt()
            val backendHour = dataArrays[3].toInt()
            if (frontHour > 0) {
                planTaskList += PlanTask(key, listOf(PlanTask.Step(GROUP_FRONT, frontHour * Constants.Time.MILLISECONDS_OF_HOUR)))
            }
            if (backendHour > 0) {
                planTaskList += PlanTask(key, listOf(PlanTask.Step(GROUP_BACKEND, backendHour * Constants.Time.MILLISECONDS_OF_HOUR)))
            }
        } else {
            println("data arrays size less than 4, it is:%s".format(dataArrays.size))
        }
        true
    }
    return planTaskList
}

fun main() {
    val projectPath = File(Constants.String.BLANK).absolutePath
    val requirementFile = File(projectPath, "planner/src/test/resources/requirement.txt")
    val planTaskList = readPlanTaskFromFile(requirementFile)
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
    Planner.plan(listOf(frontPlanLineGroup, backendPlanLineGroup), planTaskList)
    Planner.print(listOf(frontPlanLineGroup, backendPlanLineGroup), Date().getDayZeroTime())
}