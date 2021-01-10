package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.math.BigDecimal
import java.util.*

object ResourceForecaster {
    private val logger = LoggerManager.getLogger(ResourceForecaster::class)

    fun forecast(initializeAmount: Double, resourceInputItemList: List<ResourceInputItem>): List<ResourceOutputItem> {
        var beginTime: Date? = null
        var endTime: Date? = null
        resourceInputItemList.forEach {
            val calculateBeginTime = it.amountTime
            val calculateEndTime = it.amountTime
            //begin time
            val tempBeginTime = beginTime
            if (tempBeginTime == null) {
                beginTime = calculateBeginTime
            } else {
                if (calculateBeginTime.getDayZeroTime() < tempBeginTime.getDayZeroTime()) {
                    beginTime = calculateBeginTime
                }
            }
            //end time
            val tempEndTime = endTime
            if (tempEndTime == null) {
                endTime = calculateEndTime
            } else {
                if (calculateEndTime.getDayZeroTime() > tempEndTime.getDayZeroTime()) {
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
        val beginTimeLong = tempBeginTime.getDayZeroTime()
        val endTimeLong = tempEndTime.getDayZeroTime()
        val timeInterval = ((endTimeLong - beginTimeLong) / Constants.Time.MILLISECONDS_OF_DAY).toInt()
        var totalPlanShouldReceive = BigDecimal(0)//总应收
        var totalPlanShouldPay: BigDecimal = BigDecimal(0)//总应付
        var totalActualShouldReceive = BigDecimal(0)//总实际应收
        var totalActualShouldPay: BigDecimal = BigDecimal(0)//总实际应付
        var totalActualReceive = BigDecimal(0)//总实收
        var totalActualPay: BigDecimal = BigDecimal(0)//总实付
        var total = BigDecimal(initializeAmount)//总剩余
        val resultTreeMap = TreeMap<String, ResourceOutputItem>()
        val resourceInputItemListGroupMap = resourceInputItemList.groupBy { it.amountTime.toFormatString(Constants.Time.YEAR_MONTH_DAY) }
        (0..timeInterval).forEach {
            val currentDateKey = tempBeginTime.getDayZeroTimeDateNext(it).toFormatString(Constants.Time.YEAR_MONTH_DAY)
            val resourceOutputItem = resultTreeMap.getOrPut(currentDateKey) {
                ResourceOutputItem()
            }
            val resourceInputItemListGroup = resourceInputItemListGroupMap[currentDateKey] ?: emptyList()
            if (resourceInputItemListGroup.isNotEmpty()) {
                resourceInputItemListGroup.forEach { resourceInputItem ->
                    val amount = resourceInputItem.amount
                    val amountTime = resourceInputItem.amountTime
                    val dateKey = amountTime.toFormatString(Constants.Time.YEAR_MONTH_DAY)
                    val currentResourceOutputItem = resultTreeMap[dateKey] ?: error("dateKey:${dateKey} does not exist, it maybe has a bug")
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
                                    totalActualShouldReceive += amount
                                }
                                else -> {//实际应付
                                    currentResourceOutputItem.actualShouldPay += amount
                                    totalActualShouldPay += amount
                                }
                            }
                        }
                        else -> {//应plan
                            when (resourceInputItem.direction) {
                                ResourceInputItem.Direction.IN.value -> {//应收
                                    currentResourceOutputItem.planShouldReceive += amount
                                    totalPlanShouldReceive += amount
                                }
                                else -> {//应付
                                    currentResourceOutputItem.planShouldPay += amount
                                    totalPlanShouldPay += amount
                                }
                            }
                        }
                    }
                }
            }
            resourceOutputItem.apply {
                this.time = currentDateKey.toUtilDate(Constants.Time.YEAR_MONTH_DAY)
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