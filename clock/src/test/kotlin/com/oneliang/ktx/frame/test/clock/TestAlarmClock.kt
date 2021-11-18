package com.oneliang.ktx.frame.test.clock

import com.oneliang.ktx.frame.clock.AlarmClock

class Item(override val key: String, override val expiredTime: Long) : AlarmClock.Item

fun main() {
    val alarmClock = AlarmClock<Item>(1000) {
        println(it.key + "," + it.expiredTime)
    }
    alarmClock.start()
    alarmClock.addItem(Item("A", System.currentTimeMillis() + 1000))
    alarmClock.addItem(Item("B", System.currentTimeMillis() + 5000))
}