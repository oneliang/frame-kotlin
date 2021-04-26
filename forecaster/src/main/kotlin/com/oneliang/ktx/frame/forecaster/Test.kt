package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.jxl.readSimpleExcel
import com.oneliang.ktx.util.jxl.writeSimpleExcel

private fun readExcel2(): List<ResourceInputItem> {
    val resourceInputItemList = mutableListOf<ResourceInputItem>()
    val readResult = "C:/Users/Administrator/Desktop/in_out.xls".toFile().readSimpleExcel(headerRowIndex = 0)
    readResult.dataList.forEachIndexed { index, it ->
        val time = it["time"].nullToBlank()
        val inPlanShould = it["in_plan_should"].nullToBlank()
        val inPlanShouldTime = it["in_plan_should_time"].nullToBlank()
        resourceInputItemList += ResourceInputItem(
            inPlanShould.toBigDecimal(),
            ResourceInputItem.Direction.IN.value,
            ResourceInputItem.Type.PLAN_SHOULD.value,
            time.toUtilDate().getDayZeroTimeDate()
        ).apply {
            this.amountTime = inPlanShouldTime.toUtilDate().getDayZeroTimeDate()
        }
        //in actual should
        for (i in 1..20) {
            val inActualShould = it["in_actual_should_$i"].nullToBlank()
            val inActualShouldTime = it["in_actual_should_time_$i"].nullToBlank()
            if (inActualShould.isNotBlank() && inActualShouldTime.isNotBlank()) {
                resourceInputItemList += ResourceInputItem(
                    inActualShould.toBigDecimal(),
                    ResourceInputItem.Direction.IN.value,
                    ResourceInputItem.Type.ACTUAL_SHOULD.value,
                    time.toUtilDate().getDayZeroTimeDate()
                ).apply {
                    this.amountTime = inActualShouldTime.toUtilDate().getDayZeroTimeDate()
                }
            }
        }
        //in actual
        for (i in 1..20) {
            val inActual = it["in_actual_$i"].nullToBlank()
            val inActualTime = it["in_actual_time_$i"].nullToBlank()
            if (inActual.isNotBlank() && inActualTime.isNotBlank()) {
                resourceInputItemList += ResourceInputItem(
                    inActual.toBigDecimal(),
                    ResourceInputItem.Direction.IN.value,
                    ResourceInputItem.Type.ACTUAL.value,
                    time.toUtilDate().getDayZeroTimeDate()
                ).apply {
                    this.amountTime = inActualTime.toUtilDate().getDayZeroTimeDate()
                }
            }
        }
        //out actual
        for (i in 1..20) {
            val outActualShould = it["out_actual_should_$i"].nullToBlank()
            val outActualShouldTime = it["out_actual_should_time_$i"].nullToBlank()
            if (outActualShould.isNotBlank() && outActualShouldTime.isNotBlank()) {
                resourceInputItemList += ResourceInputItem(
                    outActualShould.toBigDecimal(),
                    ResourceInputItem.Direction.OUT.value,
                    ResourceInputItem.Type.ACTUAL_SHOULD.value,
                    time.toUtilDate().getDayZeroTimeDate()
                ).apply {
                    this.amountTime = outActualShouldTime.toUtilDate().getDayZeroTimeDate()
                }
            }
        }
    }
    return resourceInputItemList
}

private fun readExcel(): List<ResourceInputItem> {
    val resourceInputItemList = mutableListOf<ResourceInputItem>()
    val readResult = "C:/Users/Administrator/Desktop/test.xls".toFile().readSimpleExcel(headerRowIndex = 0)
    readResult.dataList.forEachIndexed { index, it ->
        val time = it["time"].nullToBlank()
        val inPlanShould = it["in_plan_should"].nullToBlank()
        val inPlanShouldTime = it["in_plan_should_time"].nullToBlank()
        resourceInputItemList += ResourceInputItem(
            inPlanShould.toBigDecimal(),
            ResourceInputItem.Direction.IN.value,
            ResourceInputItem.Type.PLAN_SHOULD.value,
            time.toUtilDate().getDayZeroTimeDate()
        ).apply {
            this.amountTime = inPlanShouldTime.toUtilDate().getDayZeroTimeDate()
        }
        val inActualShould = it["in_actual_should"].nullToBlank()
        val inActualShouldTime = it["in_actual_should_time"].nullToBlank()
        resourceInputItemList += ResourceInputItem(
            inActualShould.toBigDecimal(),
            ResourceInputItem.Direction.IN.value,
            ResourceInputItem.Type.ACTUAL_SHOULD.value,
            time.toUtilDate().getDayZeroTimeDate()
        ).apply {
            this.amountTime = inActualShouldTime.toUtilDate().getDayZeroTimeDate()
        }
        //in actual
        val inActual = it["in_actual"].nullToBlank()
        val inActualTime = it["in_actual_time"].nullToBlank()
        resourceInputItemList += ResourceInputItem(
            inActual.toBigDecimal(),
            ResourceInputItem.Direction.IN.value,
            ResourceInputItem.Type.ACTUAL.value,
            time.toUtilDate().getDayZeroTimeDate()
        ).apply {
            this.amountTime = inActualTime.toUtilDate().getDayZeroTimeDate()
        }
        //out actual
        val outActualShould = it["out_actual_should"].nullToBlank()
        val outActualShouldTime = it["out_actual_should_time"].nullToBlank()
        resourceInputItemList += ResourceInputItem(
            outActualShould.toBigDecimal(),
            ResourceInputItem.Direction.OUT.value,
            ResourceInputItem.Type.ACTUAL_SHOULD.value,
            time.toUtilDate().getDayZeroTimeDate()
        ).apply {
            this.amountTime = outActualShouldTime.toUtilDate().getDayZeroTimeDate()
        }
    }
    return resourceInputItemList
}

private fun writeExcel(resourceOutputItemList: List<ResourceOutputItem>) {
    val headerArray = arrayOf(
        "日期",
        "总计划应收",
        "总计划应付",
        "总实际应收",
        "总实际应付",
        "总实收",
        "总实付",
        "总计划应收差异",
        "总实际应收差异",
        "总实收差异",
        "总剩余",
        "当日计划应收",
        "当日计划应付",
        "当日实际应收",
        "当日实际应付",
        "当日实收",
        "当日实付",
        "当日计划应收差异",
        "当日实际应收差异",
        "当日实收差异",
    )
    val iterable = resourceOutputItemList.map {
        arrayOf<Any>(
            it.time.toFormatString(),
            it.totalPlanShouldReceive,
            it.totalPlanShouldPay,
            it.totalActualShouldReceive,
            it.totalActualShouldPay,
            it.totalActualReceive,
            it.totalActualPay,
            it.totalPlanShouldDifferent,
            it.totalActualShouldDifferent,
            it.totalActualDifferent,
            it.total,
            it.planShouldReceive,
            it.planShouldPay,
            it.actualShouldReceive,
            it.actualShouldPay,
            it.actualReceive,
            it.actualPay,
            it.planShouldDifferent,
            it.actualShouldDifferent,
            it.actualDifferent,
        )
    }
    "C:/Users/Administrator/Desktop/test_out.xls".toFile().writeSimpleExcel(headerArray = headerArray, iterable = iterable)
}

fun main() {
    val a = mapOf<String, String>("1" to "a", "2" to "b")
    val b = mapOf<String, String>("2" to "b", "1" to "a", "3" to "c")
    val map = mutableMapOf<Map<String, String>, Map<String, String>>()
    map.getOrPut(a) { emptyMap() }
    map.getOrPut(b) { emptyMap() }
    println(map.size)
    println(map.toSortedMap(Comparator { o1, o2 ->
        when {
            o1.size < o2.size -> {
                return@Comparator 1
            }
            o1.size > o2.size -> {
                return@Comparator -1
            }
            else -> {
                return@Comparator 0
            }
        }
    }))
    return
    val DELIVERY_DATE_45 = 45
    val RECEIVABLE_PROPORTION = 0.2
    val resourceInputItem1 = ResourceInputItem(
        100.0.toBigDecimal(),
        ResourceInputItem.Direction.IN.value,
        ResourceInputItem.Type.PLAN_SHOULD.value,
        "2021-01-01".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    ).apply {
        this.amountTime = "2021-01-01".toUtilDate(Constants.Time.YEAR_MONTH_DAY).getDayZeroTimeDateNext(DELIVERY_DATE_45)
    }
    val resourceInputItem1_deposit = ResourceInputItem(
        resourceInputItem1.amount.multiplyByBigDecimal(RECEIVABLE_PROPORTION),
        ResourceInputItem.Direction.IN.value,
        ResourceInputItem.Type.ACTUAL.value,
        "2021-01-02".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    ).apply {
        this.amountTime = "2021-01-02".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    }
    val resourceInputItem2 = ResourceInputItem(
        30.0.toBigDecimal(),
        ResourceInputItem.Direction.OUT.value,
        ResourceInputItem.Type.ACTUAL.value,
        "2021-01-02".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    ).apply {
        this.amountTime = "2021-01-02".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    }
    val resourceInputItem3 = ResourceInputItem(
        80.0.toBigDecimal(),
        ResourceInputItem.Direction.IN.value,
        ResourceInputItem.Type.ACTUAL.value,
        "2021-01-01".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    ).apply {
        this.amountTime = "2021-01-01".toUtilDate(Constants.Time.YEAR_MONTH_DAY).getDayZeroTimeDateNext(DELIVERY_DATE_45)
    }
    val resourceInputItemList = readExcel2()//listOf(resourceInputItem1, resourceInputItem1_deposit, resourceInputItem2, resourceInputItem3)
    val resourceOutputItemList = ResourceForecaster.forecast(ResourceTotalItem(), resourceInputItemList)
    resourceOutputItemList.forEach {
        println(it)
    }
    writeExcel(resourceOutputItemList)
}