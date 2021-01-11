package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.util.json.toJson
import java.math.BigDecimal
import java.util.*

class ResourceOutputItem : ResourceTotalItem() {
    companion object;
    var planShouldReceive: BigDecimal = BigDecimal(0)
        //预计应收=总费用
        set(value) {
            field = value
            this.currentPlanShouldDifferent = field - this.planShouldPay
        }
    var planShouldPay: BigDecimal = BigDecimal(0)
        //预计应付
        set(value) {
            field = value
            this.currentPlanShouldDifferent = this.planShouldReceive - field
        }
    var actualShouldReceive: BigDecimal = BigDecimal(0)
        //实际应收=总费用
        set(value) {
            field = value
            this.currentActualShouldDifferent = field - this.actualShouldPay
        }
    var actualShouldPay: BigDecimal = BigDecimal(0)
        //实际应付
        set(value) {
            field = value
            this.currentActualShouldDifferent = this.actualShouldReceive - field
        }
    var actualReceive: BigDecimal = BigDecimal(0)
        //实收
        set(value) {
            field = value
            this.currentActualDifferent = field - this.actualPay
        }
    var actualPay: BigDecimal = BigDecimal(0)
        //实付
        set(value) {
            field = value
            this.currentActualDifferent = this.actualReceive - field
        }
    var time: Date? = null
    var currentPlanShouldDifferent: BigDecimal = BigDecimal(0)//当日应收差异
    var currentActualShouldDifferent: BigDecimal = BigDecimal(0)//当日实际应收差异
    var currentActualDifferent: BigDecimal = BigDecimal(0)//当日实收差异

    override fun toString(): String {
        return this.toJson()
    }
}