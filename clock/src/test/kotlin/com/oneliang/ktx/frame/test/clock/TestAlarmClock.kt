package com.oneliang.ktx.frame.test.clock

import com.oneliang.ktx.frame.clock.AlarmClock

fun main() {
    val alarmClock = AlarmClock {
        println(System.currentTimeMillis())
    }
    alarmClock.start()
    alarmClock.addItem(AlarmClock.Item("A").apply { this.period = 2000L })
}