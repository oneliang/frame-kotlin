package com.oneliang.ktx.frame.test.calculator

import com.oneliang.ktx.frame.calculator.Calculator
import java.math.BigDecimal

fun main() {
    val itemList = mutableListOf<Calculator.Item>()
    itemList += Calculator.Item("A", BigDecimal.ONE, BigDecimal(480), addition = BigDecimal(0), discountOrder = Calculator.DiscountOrder.DISCOUNT_ADDITION_FULLADDITION)
    itemList += Calculator.Item("B", BigDecimal(0.5), BigDecimal(480), addition = BigDecimal(0), discountOrder = Calculator.DiscountOrder.DISCOUNT_ADDITION_FULLADDITION)
    val resultItemList = Calculator.calculate(itemList, fullAddition = Triple(BigDecimal(300), BigDecimal(-30), false))
    resultItemList.forEach {
        println(it.code + "," + it.result)
    }
}