package com.oneliang.ktx.frame.clock

import com.oneliang.ktx.util.common.getZeroTimeDate
import com.oneliang.ktx.util.concurrent.ResourceQueueThread
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AlarmClock<T : AlarmClock.ExpiredItem>(private val intervalTime: Long, expiredItemCallback: (expired: T) -> Unit) {
    companion object {
        private val logger = LoggerManager.getLogger(AlarmClock::class)
    }

    private val expiredItemMap = ConcurrentHashMap<String, T>()
    private val checkExpiredQueueThread = ResourceQueueThread(object : ResourceQueueThread.ResourceProcessor<Long> {
        override fun process(resource: Long) {
            this@AlarmClock.expiredItemMap.forEach { (key, item) ->
                if (resource >= item.expiredTime) {//resource is current time large than expiredTime, so expired
                    expiredItemCallback(item)
                    this@AlarmClock.expiredItemMap.remove(key)
                }
            }
        }
    })

    private var timer: Timer? = null

    fun start() {
        this.checkExpiredQueueThread.start()
        this.timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                checkExpiredQueueThread.addResource(System.currentTimeMillis())
            }
        }
        this.timer?.scheduleAtFixedRate(timerTask, Date().getZeroTimeDate(this.intervalTime), this.intervalTime)
    }

    fun interrupt() {
        this.timer?.cancel()
        this.timer = null
        this.expiredItemMap.clear()
        this.checkExpiredQueueThread.interrupt()
    }

    fun addExpiredItem(expiredItem: T) {
        if (this.expiredItemMap.containsKey(expiredItem.key)) {
            error("key exists, key:%s".format(expiredItem.key))
        }
        this.expiredItemMap[expiredItem.key] = expiredItem
    }

    fun removeExpiredItem(key: String): T? {
        return this.expiredItemMap.remove(key)
    }

    interface ExpiredItem {
        val key: String
        val expiredTime: Long
    }
}