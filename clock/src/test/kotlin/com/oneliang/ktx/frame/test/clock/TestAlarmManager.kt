package com.oneliang.ktx.frame.test.clock

import com.oneliang.ktx.frame.clock.AlarmManager

class Item(override val key: String, override val expiredTime: Long) : AlarmManager.Item

fun main() {
    val alarmManager = AlarmManager<Item>(1000) {
        println(it.key + "," + it.expiredTime)
    }
    alarmManager.start()
    alarmManager.addItem(Item("A", System.currentTimeMillis() + 1000))
    alarmManager.addItem(Item("B", System.currentTimeMillis() + 5000))
}