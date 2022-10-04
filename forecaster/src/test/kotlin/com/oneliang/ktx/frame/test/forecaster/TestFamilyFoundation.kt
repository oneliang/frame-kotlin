package com.oneliang.ktx.frame.test.forecaster

import com.oneliang.ktx.frame.forecaster.ResourceForecaster
import com.oneliang.ktx.frame.forecaster.ResourceInputItem
import com.oneliang.ktx.frame.forecaster.ResourceOutputItem
import com.oneliang.ktx.frame.forecaster.ResourceTotalItem
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.jxl.readSimpleExcel
import com.oneliang.ktx.util.jxl.writeSimpleExcel

private fun readExcel2(): List<ResourceInputItem> {
    val resourceInputItemList = mutableListOf<ResourceInputItem>()
    val readResult = "C:/Users/Administrator/Desktop/in_out.xls".toFile().readSimpleExcel(headerRowIndex = 0)
    readResult.dataList.forEachIndexed { index, it ->
        val time = it["time"].nullToBlank()
        //in actual
        val inActual = it["in_actual"].nullToBlank()
        if (inActual.isNotBlank()) {
            resourceInputItemList += ResourceInputItem(
                inActual.toBigDecimal(),
                ResourceInputItem.Direction.IN.value,
                ResourceInputItem.Type.ACTUAL.value,
                time.toUtilDate().getDayZeroTimeDate()
            ).apply {
                this.amountTime = time.toUtilDate().getDayZeroTimeDate()
            }
        }
        //out actual
        val outActual = it["out_actual"].nullToBlank()
        if (outActual.isNotBlank()) {
            resourceInputItemList += ResourceInputItem(
                outActual.toBigDecimal(),
                ResourceInputItem.Direction.OUT.value,
                ResourceInputItem.Type.ACTUAL.value,
                time.toUtilDate().getDayZeroTimeDate()
            ).apply {
                this.amountTime = time.toUtilDate().getDayZeroTimeDate()
            }
        }
    }
    return resourceInputItemList
}


private fun writeExcel(resourceOutputItemList: List<ResourceOutputItem>) {
    val headerArray = arrayOf(
        "日期",
        "总实收",
        "总实付",
        "总实收差异",
        "总剩余",
        "当日实收",
        "当日实付",
        "当日实收差异",
    )
    val iterable = resourceOutputItemList.map {
        arrayOf<Any>(
            it.time.toFormatString(),
            it.totalActualReceive,
            it.totalActualPay,
            it.totalActualDifferent,
            it.total,
            it.actualReceive,
            it.actualPay,
            it.actualDifferent,
        )
    }
    "C:/Users/Administrator/Desktop/test_out.xls".toFile().writeSimpleExcel(headers = headerArray, iterable = iterable)
}

fun main() {
    val resourceInputItemList = readExcel2()//listOf(resourceInputItem1, resourceInputItem1_deposit, resourceInputItem2, resourceInputItem3)
    val resourceOutputItemList = ResourceForecaster.forecast(ResourceTotalItem(), resourceInputItemList)
    resourceOutputItemList.forEach {
        println(it)
    }
    writeExcel(resourceOutputItemList)
}