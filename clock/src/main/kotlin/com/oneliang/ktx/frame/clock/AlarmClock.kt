package com.oneliang.ktx.frame.clock

import com.oneliang.ktx.util.logging.LoggerManager

class AlarmClock(private val onTimeItemCallback: (item: Item) -> Unit) {
    companion object {
        private val logger = LoggerManager.getLogger(AlarmClock::class)
        private const val FREQUENCY = 1000L
    }

    private var alarmManager: AlarmManager<Item>? = null

    @Synchronized
    fun start() {
        this.alarmManager = AlarmManager(FREQUENCY) {
            if (it.period > 0) {
                addItem(it)
            }
            this@AlarmClock.onTimeItemCallback(it)
        }
        this.alarmManager?.start()
    }

    @Synchronized
    fun stop() {
        this.alarmManager?.stop()
    }

    fun addItem(item: Item) {
        if (item.period <= 0) {
            if (item.expiredTime <= 0) {
                error("expiredTime must bigger than 0 when period less than or equal 0")
            } else {
                //normal
            }
        } else {//when time.period>0, update expiredTime
            var beginTime = System.currentTimeMillis()
            beginTime -= beginTime % 1000
            item.expiredTime = beginTime + item.period
        }
        this.alarmManager?.addItem(item)
    }

    open class Item(override val key: String) : AlarmManager.Item {
        override var expiredTime: Long = 0L
        var period = 0L
    }
}