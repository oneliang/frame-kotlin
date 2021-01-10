package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*

fun main() {
    val DELIVERY_DATE_45 = 45
    val RECEIVABLE_PROPORTION = 0.2
    val resourceInputItem1 = ResourceInputItem(
        100.0.toBigDecimal(),
        ResourceInputItem.Direction.IN.value,
        ResourceInputItem.Type.PLAN_SHOULD.value
    ).apply {
        this.amountTime = "2021-01-01".toUtilDate(Constants.Time.YEAR_MONTH_DAY).getDayZeroTimeDateNext(DELIVERY_DATE_45)
    }
    val resourceInputItem1_deposit = ResourceInputItem(
        resourceInputItem1.amount.multiplyByBigDecimal(RECEIVABLE_PROPORTION),
        ResourceInputItem.Direction.IN.value,
        ResourceInputItem.Type.ACTUAL.value
    ).apply {
        this.amountTime = "2021-01-02".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    }
    val resourceInputItem2 = ResourceInputItem(
        30.0.toBigDecimal(),
        ResourceInputItem.Direction.OUT.value,
        ResourceInputItem.Type.ACTUAL.value
    ).apply {
        this.amountTime = "2021-01-02".toUtilDate(Constants.Time.YEAR_MONTH_DAY)
    }
    val resourceInputItemList = listOf(resourceInputItem1, resourceInputItem1_deposit, resourceInputItem2)
    val resourceOutputItemList = ResourceForecaster.forecast(10.0, resourceInputItemList)
    resourceOutputItemList.forEach {
        println(it)
    }
}