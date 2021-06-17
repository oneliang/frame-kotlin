package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.math.BigDecimal
import java.util.*

object ResourceForecaster {
    private val logger = LoggerManager.getLogger(ResourceForecaster::class)

    fun forecast(initializeResourceTotalItem: ResourceTotalItem, resourceInputItemList: List<ResourceInputItem>, modulusTime: Long = Constants.Time.MILLISECONDS_OF_DAY): List<ResourceOutputItem> {
        var beginTime: Date? = null
        var endTime: Date? = null
        resourceInputItemList.forEach {
            val (minTime, maxTime) = it.calculateMinAndMaxTime(modulusTime)
            val calculateBeginTime = minTime
            val calculateEndTime = maxTime
            //begin time
            val tempBeginTime = beginTime
            if (tempBeginTime == null) {
                beginTime = calculateBeginTime
            } else {
                if (calculateBeginTime.getZeroTime(modulusTime) < tempBeginTime.getZeroTime(modulusTime)) {
                    beginTime = calculateBeginTime
                }
            }
            //end time
            val tempEndTime = endTime
            if (tempEndTime == null) {
                endTime = calculateEndTime
            } else {
                if (calculateEndTime.getZeroTime(modulusTime) > tempEndTime.getZeroTime(modulusTime)) {
                    endTime = calculateEndTime
                }
            }
        }
        logger.info("begin time:%s", beginTime)
        logger.info("end time:%s", endTime)
        val tempBeginTime = beginTime
        val tempEndTime = endTime
        if (tempBeginTime == null || tempEndTime == null) {
            error("all time is null in resource")
        }
        val beginTimeLong = tempBeginTime.getZeroTime(modulusTime)
        val endTimeLong = tempEndTime.getZeroTime(modulusTime)
        val timeInterval = ((endTimeLong - beginTimeLong) / modulusTime).toInt()
        var totalPlanShouldReceive = BigDecimal(initializeResourceTotalItem.totalPlanShouldReceive.toString())//总应收
        var totalPlanShouldPay = BigDecimal(initializeResourceTotalItem.totalPlanShouldPay.toString())//总应付
        var totalActualShouldReceive = BigDecimal(initializeResourceTotalItem.totalActualShouldReceive.toString())//总实际应收
        var totalActualShouldPay = BigDecimal(initializeResourceTotalItem.totalActualShouldPay.toString())//总实际应付
        var totalActualReceive = BigDecimal(initializeResourceTotalItem.totalActualReceive.toString())//总实收
        var totalActualPay = BigDecimal(initializeResourceTotalItem.totalActualPay.toString())//总实付
        var total = BigDecimal(initializeResourceTotalItem.total.toString())//总剩余
        val resultTreeMap = TreeMap<String, ResourceOutputItem>()
        val groupByArray = resourceInputItemList.groupByMultiKeySelector(
            arrayOf({ it.time.getZeroTimeDate(modulusTime).toFormatString(Constants.Time.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLISECOND) },
                { it.amountTime.getZeroTimeDate(modulusTime).toFormatString(Constants.Time.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLISECOND) })
        )
        val groupByTime = groupByArray[0]
        val groupByAmountTime = groupByArray[1]
        (0..timeInterval).forEach {
            val currentTimeKey = tempBeginTime.getZeroTimeDateNext(modulusTime, it).toFormatString(Constants.Time.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLISECOND)
            val resourceOutputItem = resultTreeMap.getOrPut(currentTimeKey) {
                ResourceOutputItem()
            }
            val resourceInputItemListForTime = groupByTime[currentTimeKey] ?: emptyList()
            //用于计算总实应收/总实应付/总计划应收/总计划应付
            if (resourceInputItemListForTime.isNotEmpty()) {
                resourceInputItemListForTime.forEach { resourceInputItem ->
                    val amount = resourceInputItem.amount
                    when (resourceInputItem.type) {
                        ResourceInputItem.Type.ACTUAL_SHOULD.value -> {//用于实际应收
                            when (resourceInputItem.direction) {
                                ResourceInputItem.Direction.IN.value -> {//实际应收
                                    totalActualShouldReceive += amount
                                }
                                else -> {//实际应付
                                    totalActualShouldPay += amount
                                }
                            }
                        }
                        ResourceInputItem.Type.PLAN_SHOULD.value -> {//计划应收
                            when (resourceInputItem.direction) {
                                ResourceInputItem.Direction.IN.value -> {//应收
                                    totalPlanShouldReceive += amount
                                }
                                else -> {//应付
                                    totalPlanShouldPay += amount
                                }
                            }
                        }
                    }
                }
            }
            val resourceInputItemListForAmountTime = groupByAmountTime[currentTimeKey] ?: emptyList()
            if (resourceInputItemListForAmountTime.isNotEmpty()) {
                resourceInputItemListForAmountTime.forEach { resourceInputItem ->
                    val amount = resourceInputItem.amount
                    val amountTime = resourceInputItem.amountTime
                    val timeKey = amountTime.getZeroTimeDate(modulusTime).toFormatString(Constants.Time.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLISECOND)
                    val currentResourceOutputItem = resultTreeMap[timeKey] ?: error("dateKey:${timeKey} does not exist, it maybe has a bug")
                    when (resourceInputItem.type) {
                        ResourceInputItem.Type.ACTUAL.value -> {//实
                            when (resourceInputItem.direction) {
                                ResourceInputItem.Direction.IN.value -> {//实收
                                    currentResourceOutputItem.actualReceive += amount
                                    totalActualReceive += amount
                                    total += amount
                                }
                                else -> {//实付
                                    currentResourceOutputItem.actualPay += amount
                                    totalActualPay += amount
                                    total -= amount
                                }
                            }
                        }
                        ResourceInputItem.Type.ACTUAL_SHOULD.value -> {//用于实际应收
                            when (resourceInputItem.direction) {
                                ResourceInputItem.Direction.IN.value -> {//实际应收
                                    currentResourceOutputItem.actualShouldReceive += amount
                                }
                                else -> {//实际应付
                                    currentResourceOutputItem.actualShouldPay += amount
                                }
                            }
                        }
                        else -> {//计划应该
                            when (resourceInputItem.direction) {
                                ResourceInputItem.Direction.IN.value -> {//应收
                                    currentResourceOutputItem.planShouldReceive += amount
                                }
                                else -> {//应付
                                    currentResourceOutputItem.planShouldPay += amount
                                }
                            }
                        }
                    }
                }
            }
            resourceOutputItem.apply {
                this.time = currentTimeKey.toUtilDate(Constants.Time.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLISECOND)
                this.totalPlanShouldReceive = totalPlanShouldReceive
                this.totalPlanShouldPay = totalPlanShouldPay
                this.totalActualShouldReceive = totalActualShouldReceive
                this.totalActualShouldPay = totalActualShouldPay
                this.totalActualReceive = totalActualReceive
                this.totalActualPay = totalActualPay
                this.total = total
            }
        }
        return resultTreeMap.toList { _, value -> value }
    }
}