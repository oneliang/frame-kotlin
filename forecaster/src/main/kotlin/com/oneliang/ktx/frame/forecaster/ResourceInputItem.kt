package com.oneliang.ktx.frame.forecaster

import com.oneliang.ktx.util.json.toJson
import java.math.BigDecimal
import java.util.*

class ResourceInputItem(
    var amount: BigDecimal = BigDecimal(0),
    var direction: Int = Direction.IN.value,
    var type: Int = Type.PLAN_SHOULD.value
) {
    var amountTime = Date()//时间

    enum class Direction(val value: Int) {
        IN(0), OUT(1)
    }

    enum class Type(val value: Int) {
        PLAN_SHOULD(0),//计划应该,用于计划应收/应付
        ACTUAL_SHOULD(1), //实际应该,用于实际应收/应付
        ACTUAL(2)//实际,实际收付
    }

//    fun calculateMinAndMaxTime(): Pair<Date?, Date?> {
//        val receivableTimeLong = this.receivableTime?.getDayZeroTime()
//        val payableTimeLong = this.paidTime?.getDayZeroTime()
//        var calculateBeginTime: Date? = null
//        var calculateEndTime: Date? = null
//        if (receivableTimeLong != null) {
//            calculateBeginTime = this.receivableTime
//            calculateEndTime = this.receivableTime
//        }
//        //calculate begin
//        if (calculateBeginTime == null) {
//            calculateBeginTime = this.paidTime
//        } else {
//            if (payableTimeLong != null) {
//                if (calculateBeginTime.getDayZeroTime() > payableTimeLong) {
//                    calculateBeginTime = this.paidTime
//                }
//            }
//        }
//        //calculate end
//        if (calculateEndTime == null) {
//            calculateEndTime = this.paidTime
//        } else {
//            if (payableTimeLong != null) {
//                if (calculateEndTime.getDayZeroTime() < payableTimeLong) {
//                    calculateEndTime = this.paidTime
//                }
//            }
//        }
//        return calculateBeginTime to calculateEndTime
//    }

    override fun toString(): String {
        return this.toJson()
    }
}