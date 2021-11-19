package com.oneliang.ktx.frame.test.clock

import com.oneliang.ktx.frame.clock.AlarmClock

fun main() {
    val alarmClock = AlarmClock {
        println(it.key + "," + System.currentTimeMillis())
    }
    alarmClock.start()
    alarmClock.addItem(AlarmClock.Item("A").apply { this.period = 2000L })
    alarmClock.addItem(AlarmClock.Item("B").apply { this.expiredTime = System.currentTimeMillis() + 5000 })
}