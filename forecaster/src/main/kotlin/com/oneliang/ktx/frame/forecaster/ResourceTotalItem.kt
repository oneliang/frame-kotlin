package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.util.json.toJson
import java.math.BigDecimal
import java.util.*

open class ResourceTotalItem {
    companion object

    var totalPlanShouldReceive: BigDecimal = BigDecimal(0)
        //总计划应收
        set(value) {
            field = value
            this.totalPlanShouldDifferent = field - this.totalPlanShouldPay
        }
    var totalPlanShouldPay: BigDecimal = BigDecimal(0)
        //总计划应付
        set(value) {
            field = value
            this.totalPlanShouldDifferent = this.totalPlanShouldReceive - field
        }
    var totalActualShouldReceive: BigDecimal = BigDecimal(0)
        //总实际应收
        set(value) {
            field = value
            this.totalActualShouldDifferent = field - this.totalActualShouldPay
        }
    var totalActualShouldPay: BigDecimal = BigDecimal(0)
        //总实际应付
        set(value) {
            field = value
            this.totalActualShouldDifferent = this.totalActualShouldReceive - field
        }
    var totalActualReceive: BigDecimal = BigDecimal(0)
        //总实收
        set(value) {
            field = value
            this.totalActualDifferent = field - this.totalActualPay
        }
    var totalActualPay: BigDecimal = BigDecimal(0)
        //总实付
        set(value) {
            field = value
            this.totalActualDifferent = this.totalActualReceive - field
        }
    var totalPlanShouldDifferent: BigDecimal = BigDecimal(0)//总计划应收差异
    var totalActualShouldDifferent: BigDecimal = BigDecimal(0)//总实际应收差异
    var totalActualDifferent: BigDecimal = BigDecimal(0)//总实收差异
    var total: BigDecimal = BigDecimal(0)//总剩余

    override fun toString(): String {
        return this.toJson()
    }
}