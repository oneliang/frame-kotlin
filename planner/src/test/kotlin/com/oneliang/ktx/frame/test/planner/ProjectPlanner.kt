package com.oneliang.ktx.frame.test.planner

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.planner.*
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.file.readContentEachLine
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.*

const val GROUP_FRONT = "front"
const val GROUP_BACKEND = "backend"

private val holidaySet = hashSetOf("2022-09-12")
private val workdaySet = hashSetOf("2022-09-03")
private fun Date.isHoliday(): Boolean {
    val dateString = this.toFormatString(Constants.Time.YEAR_MONTH_DAY)
    if (workdaySet.contains(dateString)) {//first check workday
        return false
    } else if (holidaySet.contains(dateString)) {
        return true
    } else {//normal workday
        val dayOfWeek = this.toCalendar().getDayOfWeek()
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return true
        }
    }
    return false
}

private fun generatePlanTimeList(beginDate: Date, days: Int): List<PlanTime> {
    val planTimeList = mutableListOf<PlanTime>()
    for (day in 0 until days) {
        val date = beginDate.getDayZeroTimeDateNext(day)
        if (date.isHoliday()) {
            continue
        }
        planTimeList += PlanTime((day * 24 + 9) * Constants.Time.MILLISECONDS_OF_HOUR, (day * 24 + 17) * Constants.Time.MILLISECONDS_OF_HOUR)
    }
    return planTimeList
}

private fun generatePlanLine(key: String, planTimeList: List<PlanTime>): PlanLine {
    return PlanLine(key).also { it.planTimeList = planTimeList }
}

private fun generatePlanLineList(keys: Array<String>, planTimeList: List<PlanTime>): List<PlanLine> {
    val planLineList = mutableListOf<PlanLine>()
    for (key in keys) {
        planLineList += PlanLine(key).also {
            it.planTimeList = planTimeList
        }
    }
    return planLineList
}

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
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val projectPath = File(Constants.String.BLANK).absolutePath
    val frontAKey = "front_a"
    val frontBKey = "front_b"
    val backendAKey = "backend_a"
    val backendBKey = "backend_b"
    val frontAPlanTimeBeginDate = "2022-08-25 00:00:00".toUtilDate()
    val frontBPlanTimeBeginDate = "2022-08-25 00:00:00".toUtilDate()
    val backendAPlanTimeBeginDate = "2022-08-25 00:00:00".toUtilDate()
    val backendBPlanTimeBeginDate = "2022-09-08 00:00:00".toUtilDate()
    val frontAPlanTimeList = generatePlanTimeList(frontAPlanTimeBeginDate, 100)
    val frontBPlanTimeList = generatePlanTimeList(frontBPlanTimeBeginDate, 100)
    val backendAPlanTimeList = generatePlanTimeList(backendAPlanTimeBeginDate, 100)
    val backendBPlanTimeList = generatePlanTimeList(backendBPlanTimeBeginDate, 100)
    //2.2.0
    val beginDate220 = "2022-08-25 00:00:00".toUtilDate()
    val requirement220File = File(projectPath, "planner/src/test/resources/2.2.0_requirement.txt")
    val requirement220PlanTaskList = readPlanTaskFromFile(requirement220File)
    val front220PlanLineGroup = PlanLineGroup(GROUP_FRONT)
    val backend220PlanLineGroup = PlanLineGroup(GROUP_BACKEND)
    front220PlanLineGroup.planLineList = listOf(
        generatePlanLine(frontAKey, frontAPlanTimeList)
    )
    backend220PlanLineGroup.planLineList = listOf(
        generatePlanLine(backendAKey, backendAPlanTimeList)
    )
    Planner.plan(listOf(front220PlanLineGroup, backend220PlanLineGroup), requirement220PlanTaskList)
    Planner.print(listOf(front220PlanLineGroup, backend220PlanLineGroup), beginDate220.time)
    println("--------------------")
    //2.2.1
    val beginDate221 = "2022-08-31 00:00:00".toUtilDate()
    val requirement221File = File(projectPath, "planner/src/test/resources/2.2.1_requirement.txt")
    val requirement221PlanTaskList = readPlanTaskFromFile(requirement221File)
    val front221PlanLineGroup = PlanLineGroup(GROUP_FRONT)
    val backend221PlanLineGroup = PlanLineGroup(GROUP_BACKEND)
    front221PlanLineGroup.planLineList = listOf(
        generatePlanLine(frontAKey, frontAPlanTimeList),
        generatePlanLine(frontBKey, frontBPlanTimeList)
    )
    backend221PlanLineGroup.planLineList = listOf(
        generatePlanLine(backendAKey, backendAPlanTimeList),
        generatePlanLine(backendBKey, backendBPlanTimeList)
    )
    Planner.plan(listOf(front221PlanLineGroup, backend221PlanLineGroup), requirement221PlanTaskList)
    Planner.print(listOf(front221PlanLineGroup, backend221PlanLineGroup), beginDate221.time)
    println("--------------------")
    //2.2.2
    val beginDate222 = "2022-09-16 00:00:00".toUtilDate()
    val requirement222File = File(projectPath, "planner/src/test/resources/2.2.2_requirement.txt")
    val requirement222PlanTaskList = readPlanTaskFromFile(requirement222File)
    val front222PlanLineGroup = PlanLineGroup(GROUP_FRONT)
    val backend222PlanLineGroup = PlanLineGroup(GROUP_BACKEND)
    front222PlanLineGroup.planLineList = listOf(
        generatePlanLine(frontAKey, frontAPlanTimeList),
        generatePlanLine(frontBKey, frontBPlanTimeList),
    )
    backend222PlanLineGroup.planLineList = listOf(
        generatePlanLine(backendAKey, backendAPlanTimeList),
        generatePlanLine(backendBKey, backendBPlanTimeList)
    )
    Planner.plan(listOf(front222PlanLineGroup, backend222PlanLineGroup), requirement222PlanTaskList)
    Planner.print(listOf(front222PlanLineGroup, backend222PlanLineGroup), beginDate222.time)
}