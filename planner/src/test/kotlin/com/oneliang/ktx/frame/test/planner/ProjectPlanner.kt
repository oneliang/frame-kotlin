package com.oneliang.ktx.frame.test.planner

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.planner.*
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.file.readContentEachLine
import com.oneliang.ktx.util.jxl.writeSimpleExcel
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.*

const val GROUP_FRONT = "front"
const val GROUP_BACKEND = "backend"

private val holidaySet = hashSetOf("2022-09-12")
private val workdaySet = hashSetOf("2022-09-03", "2022-09-17")
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
//        println("day:$day," + date.toFormatString())
        planTimeList += PlanTime((day * 24 + 9) * Constants.Time.MILLISECONDS_OF_HOUR, (day * 24 + 17) * Constants.Time.MILLISECONDS_OF_HOUR)
//        println((beginDate.getDayZeroTime() + (day * 24 + 9) * Constants.Time.MILLISECONDS_OF_HOUR).toUtilDate().toFormatString())
    }
    return planTimeList
}

private fun generatePlanLine(key: String, beginDate: Date, planTimeList: List<PlanTime>): PlanLine {
    return PlanLine(key, beginDate.time).also { it.planTimeList = planTimeList }
}

private fun generatePlanLine(key: String, beginDate: Date, days: Int): PlanLine {
    return generatePlanLine(key, beginDate, generatePlanTimeList(beginDate, days))
}

private fun generatePlanLineList(keys: Array<String>, beginTime: Long, planTimeList: List<PlanTime>): List<PlanLine> {
    val planLineList = mutableListOf<PlanLine>()
    for (key in keys) {
        planLineList += PlanLine(key, beginTime).also {
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

private val frontAKey = "front_a"
private val frontBKey = "front_b"
private val backendAKey = "backend_a"
private val backendBKey = "backend_b"
private fun testPlanFor220(projectPath: String) {
    val frontAPlanTimeBeginDate = "2022-08-25 00:00:00".toUtilDate()
    val backendAPlanTimeBeginDate = "2022-08-25 00:00:00".toUtilDate()
    //2.2.0
    val requirement220File = File(projectPath, "planner/src/test/resources/2.2.0_requirement.txt")
    val requirement220PlanTaskList = readPlanTaskFromFile(requirement220File)
    val front220PlanLineGroup = PlanLineGroup(GROUP_FRONT)
    val backend220PlanLineGroup = PlanLineGroup(GROUP_BACKEND)
    front220PlanLineGroup.planLineList = listOf(
        generatePlanLine(frontAKey, frontAPlanTimeBeginDate, 100)
    )
    backend220PlanLineGroup.planLineList = listOf(
        generatePlanLine(backendAKey, backendAPlanTimeBeginDate, 100)
    )
//    Planner.plan(listOf(front220PlanLineGroup, backend220PlanLineGroup), requirement220PlanTaskList)
//    Planner.print(listOf(front220PlanLineGroup, backend220PlanLineGroup), beginDate220.time)
}

private fun testPlanFor221(projectPath: String) {
    val frontAPlanTimeBeginDate = "2022-09-01 00:00:00".toUtilDate()
    val frontBPlanTimeBeginDate = "2022-09-01 00:00:00".toUtilDate()
    val backendAPlanTimeBeginDate = "2022-09-01 00:00:00".toUtilDate()
    val backendBPlanTimeBeginDate = "2022-09-01 00:00:00".toUtilDate()
    val requirement221File = File(projectPath, "planner/src/test/resources/2.2.1_requirement.txt")
    val requirement221PlanTaskList = readPlanTaskFromFile(requirement221File)
    val front221PlanLineGroup = PlanLineGroup(GROUP_FRONT)
    val backend221PlanLineGroup = PlanLineGroup(GROUP_BACKEND)
    front221PlanLineGroup.planLineList = listOf(
        generatePlanLine(frontAKey, frontAPlanTimeBeginDate, 100),
        generatePlanLine(frontBKey, frontBPlanTimeBeginDate, 100)
    )
    backend221PlanLineGroup.planLineList = listOf(
        generatePlanLine(backendAKey, backendAPlanTimeBeginDate, 100),
        generatePlanLine(backendBKey, backendBPlanTimeBeginDate, 100)
    )
    Planner.plan(listOf(front221PlanLineGroup, backend221PlanLineGroup), requirement221PlanTaskList)
    Planner.print(listOf(front221PlanLineGroup, backend221PlanLineGroup))
}

private fun testPlanFor222(projectPath: String): List<PlanLineGroup> {
    val frontAPlanTimeBeginDate = "2022-09-16 00:00:00".toUtilDate()
    val frontBPlanTimeBeginDate = "2022-09-16 00:00:00".toUtilDate()
    val backendAPlanTimeBeginDate = "2022-09-16 00:00:00".toUtilDate()
    val backendBPlanTimeBeginDate = "2022-09-16 00:00:00".toUtilDate()
    val requirement222File = File(projectPath, "planner/src/test/resources/2.2.2_requirement.txt")
    val requirement222PlanTaskList = readPlanTaskFromFile(requirement222File)
    val front222PlanLineGroup = PlanLineGroup(GROUP_FRONT)
    val backend222PlanLineGroup = PlanLineGroup(GROUP_BACKEND)
    front222PlanLineGroup.planLineList = listOf(
        generatePlanLine(frontAKey, frontAPlanTimeBeginDate, 100),
        generatePlanLine(frontBKey, frontBPlanTimeBeginDate, 100),
    )
    backend222PlanLineGroup.planLineList = listOf(
        generatePlanLine(backendAKey, backendAPlanTimeBeginDate, 100),
        generatePlanLine(backendBKey, backendBPlanTimeBeginDate, 100)
    )
    val planLineGorupList = listOf(front222PlanLineGroup, backend222PlanLineGroup)
    Planner.plan(planLineGorupList, requirement222PlanTaskList)
    println("----------print the plan----------")
    Planner.print(listOf(front222PlanLineGroup, backend222PlanLineGroup), true)
    return planLineGorupList
}

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val projectPath = File(Constants.String.BLANK).absolutePath
    testPlanFor220(projectPath)
    println("--------------------")
    //2.2.1
    testPlanFor221(projectPath)
    println("--------------------")
    //2.2.2
    val planLineGroupList = testPlanFor222(projectPath)
    writeExcel(planLineGroupList)
}

private fun writeExcel(planLineGroupList: List<PlanLineGroup>) {

    val headerArray = arrayOf(
        "group_key",
        "line_name",
        "任务名称",
        "开始时间",
        "结束时间"
    )
    val dataList = mutableListOf<Array<Any>>()
    planLineGroupList.forEach { planLineGroup ->
        val planLineList = planLineGroup.planLineList
        planLineList.forEach { planLine ->
            planLine.planItemList.forEach { planItem ->
                val planTask = planItem.planTask
                val planTaskStep = planItem.planTaskStep
                val planBeginDateString = (planLine.beginTime + planItem.planBeginTime).toUtilDate().toFormatString()
                val planEndDateString = (planLine.beginTime + planItem.planEndTime).toUtilDate().toFormatString()
                dataList += arrayOf(
                    planLineGroup.key,
                    planLine.name,
                    planTask.key,
                    planBeginDateString,
                    planEndDateString
                )
            }
        }

    }
    "C:/Users/Administrator/Desktop/project_planner.xls".toFile().writeSimpleExcel(headers = headerArray, iterable = dataList)
}