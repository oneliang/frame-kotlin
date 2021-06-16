package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.util.json.toJson
import java.math.BigDecimal
import java.util.*

class ResourceOutputItem : ResourceTotalItem() {
    companion object;
    var planShouldReceive: BigDecimal = BigDecimal(0)
        //当日计划应收=总费用
        set(value) {
            field = value
            this.planShouldDifferent = field - this.planShouldPay
        }
    var planShouldPay: BigDecimal = BigDecimal(0)
        //当日计划应付
        set(value) {
            field = value
            this.planShouldDifferent = this.planShouldReceive - field
        }
    var actualShouldReceive: BigDecimal = BigDecimal(0)
        //当日实际应收=总费用
        set(value) {
            field = value
            this.actualShouldDifferent = field - this.actualShouldPay
        }
    var actualShouldPay: BigDecimal = BigDecimal(0)
        //当日实际应付
        set(value) {
            field = value
            this.actualShouldDifferent = this.actualShouldReceive - field
        }
    var actualReceive: BigDecimal = BigDecimal(0)
        //当日实收
        set(value) {
            field = value
            this.actualDifferent = field - this.actualPay
        }
    var actualPay: BigDecimal = BigDecimal(0)
        //当日实付
        set(value) {
            field = value
            this.actualDifferent = this.actualReceive - field
        }
    lateinit var time: Date
    var planShouldDifferent: BigDecimal = BigDecimal(0)//当日应收差异
    var actualShouldDifferent: BigDecimal = BigDecimal(0)//当日实际应收差异
    var actualDifferent: BigDecimal = BigDecimal(0)//当日实收差异

    override fun toString(): String {
        return this.toJson()
    }
}