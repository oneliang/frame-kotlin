package com.oneliang.ktx.frame.calculator

import java.math.BigDecimal
import java.math.RoundingMode

object Calculator {
    enum class DiscountOrder(val value: Int) {
        DISCOUNT_ADDITION_FULLADDITION(0),
        DISCOUNT_FULLADDITION_ADDITION(1),
        ADDITION_DISCOUNT_FULLADDITION(2),
        ADDITION_FULLADDITION_DISCOUNT(3),
        FULLADDITION_DISCOUNT_ADDITION(4),
        FULLADDITION_ADDITION_DISCOUNT(5),
    }

    fun calculate(itemList: List<Item>, discount: BigDecimal = BigDecimal.ONE, addition: BigDecimal = BigDecimal.ZERO, fullAddition: Triple<BigDecimal, BigDecimal, Boolean> = Triple(BigDecimal.ZERO, BigDecimal.ZERO, false), discountOrder: DiscountOrder = DiscountOrder.DISCOUNT_ADDITION_FULLADDITION): List<ResultItem> {
        val resultItemList = mutableListOf<ResultItem>()
        var total = BigDecimal.ZERO
        itemList.forEach {
            val result = it.calculate()
            val resultItem = it.toResultItem(result)
            total += result
            resultItemList += resultItem
        }
        val resultItem = ResultItem(ResultItem.TOTAL_RESULT_ITEM_CODE, BigDecimal.ONE, total, discount, addition, fullAddition, discountOrder)
        resultItem.result = resultItem.calculate()
        resultItemList += resultItem
        return resultItemList
    }

    open class Item(
        var code: String,
        var count: BigDecimal,
        var unitPrice: BigDecimal,
        var discount: BigDecimal = BigDecimal.ONE,//折扣
        var addition: BigDecimal = BigDecimal.ZERO,//优惠券,一次性加减
        var fullAddition: Triple<BigDecimal, BigDecimal, Boolean> = Triple(BigDecimal.ZERO, BigDecimal.ZERO, false),//满加减, true每满减, false仅一次满减
        var discountOrder: DiscountOrder = DiscountOrder.DISCOUNT_ADDITION_FULLADDITION
    ) {

        private fun fullAdditionCalculate(total: BigDecimal, fullAddition: Triple<BigDecimal, BigDecimal, Boolean>): BigDecimal {
            val full = fullAddition.first
            val addition = fullAddition.second
            val sign = fullAddition.third
            return if (full.compareTo(BigDecimal.ZERO) == 0) {
                total
            } else {
                val fullAdditionCount = total.divide(full.abs(), 0, RoundingMode.FLOOR)//0时不足满减条件
                if (sign) {
                    total + (fullAdditionCount * addition)
                } else {
                    if (fullAdditionCount >= BigDecimal.ONE) {
                        total + (BigDecimal.ONE * addition)
                    } else {
                        total
                    }
                }
            }
        }

        fun calculate(): BigDecimal {
            val current = count * unitPrice
            return when (discountOrder) {
                DiscountOrder.DISCOUNT_ADDITION_FULLADDITION -> {
                    val tempTotal = current * discount + addition
                    fullAdditionCalculate(tempTotal, fullAddition)
                }
                DiscountOrder.DISCOUNT_FULLADDITION_ADDITION -> {
                    val tempTotal = current * discount
                    fullAdditionCalculate(tempTotal, fullAddition) + addition
                }
                DiscountOrder.ADDITION_DISCOUNT_FULLADDITION -> {
                    val tempTotal = (current + addition) * discount
                    fullAdditionCalculate(tempTotal, fullAddition)
                }
                DiscountOrder.ADDITION_FULLADDITION_DISCOUNT -> {
                    val tempTotal = current + addition
                    fullAdditionCalculate(tempTotal, fullAddition) * discount
                }
                DiscountOrder.FULLADDITION_DISCOUNT_ADDITION -> {
                    fullAdditionCalculate(current, fullAddition) * discount + addition
                }
                DiscountOrder.FULLADDITION_ADDITION_DISCOUNT -> {
                    (fullAdditionCalculate(current, fullAddition) + addition) * discount
                }
            }
        }
    }

    class ResultItem(
        code: String,
        count: BigDecimal,
        unitPrice: BigDecimal,
        discount: BigDecimal = BigDecimal.ZERO,//折扣
        addition: BigDecimal = BigDecimal.ZERO,//优惠券,一次性加减
        fullAddition: Triple<BigDecimal, BigDecimal, Boolean> = Triple(BigDecimal.ZERO, BigDecimal.ZERO, false),//满加减, true每满减, false仅一次满减
        discountOrder: DiscountOrder = DiscountOrder.DISCOUNT_ADDITION_FULLADDITION
    ) : Item(
        code, count, unitPrice, discount, addition, fullAddition, discountOrder
    ) {
        companion object {
            const val TOTAL_RESULT_ITEM_CODE = "TOTAL"
        }

        var result: BigDecimal = BigDecimal.ZERO
    }
}

fun Calculator.Item.toResultItem(result: BigDecimal): Calculator.ResultItem {
    val resultItem = Calculator.ResultItem(this.code, this.count, this.unitPrice, this.discount, this.addition, this.fullAddition, this.discountOrder)
    resultItem.result = result
    return resultItem
}