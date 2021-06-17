package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.util.common.getZeroTime
import com.oneliang.ktx.util.json.toJson
import java.math.BigDecimal
import java.util.*

class ResourceInputItem(
    var amount: BigDecimal = BigDecimal(0),
    var direction: Int = Direction.IN.value,
    var type: Int = Type.PLAN_SHOULD.value,
    var time: Date = Date()//数据输入时间,用于totalXxxShould等数据计算
) {
    var amountTime = Date()//对应amount时发生的时间

    enum class Direction(val value: Int) {
        IN(0), OUT(1)
    }

    enum class Type(val value: Int) {
        PLAN_SHOULD(0),//计划应该,用于计划应收/应付
        ACTUAL_SHOULD(1), //实际应该,用于实际应收/应付
        ACTUAL(2)//实际,实际收付
    }

    fun calculateMinAndMaxTime(modulusTime: Long): Pair<Date, Date> {
        val timeLong = this.time.getZeroTime(modulusTime)
        val amountTimeLong = this.amountTime.getZeroTime(modulusTime)
        return if (timeLong > amountTimeLong) {
            this.amountTime to this.time
        } else {
            this.time to this.amountTime
        }
    }

    override fun toString(): String {
        return this.toJson()
    }
}