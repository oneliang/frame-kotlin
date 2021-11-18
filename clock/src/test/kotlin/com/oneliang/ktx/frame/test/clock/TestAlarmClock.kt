package com.oneliang.ktx.frame.test.clock

import com.oneliang.ktx.frame.clock.AlarmClock

class Item(override val key: String, override val expiredTime: Long) : AlarmClock.ExpiredItem

fun main() {
    val alarmClock = AlarmClock<Item>(1000) {
        println(it.key + "," + it.expiredTime)
    }
    alarmClock.start()
    alarmClock.addExpiredItem(Item("A", System.currentTimeMillis() + 1000))
    alarmClock.addExpiredItem(Item("B", System.currentTimeMillis() + 5000))
}