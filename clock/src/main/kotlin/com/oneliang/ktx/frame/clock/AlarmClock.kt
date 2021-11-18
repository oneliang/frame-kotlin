package com.oneliang.ktx.frame.clock

import com.oneliang.ktx.util.common.getZeroTimeDate
import com.oneliang.ktx.util.concurrent.ResourceQueueThread
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AlarmClock<T : AlarmClock.Item>(private val intervalTime: Long, expiredItemCallback: (item: T) -> Unit) {
    companion object {
        private val logger = LoggerManager.getLogger(AlarmClock::class)
    }

    private val itemMap = ConcurrentHashMap<String, T>()
    private val checkExpiredQueueThread = ResourceQueueThread(object : ResourceQueueThread.ResourceProcessor<Long> {
        override fun process(resource: Long) {
            this@AlarmClock.itemMap.forEach { (key, item) ->
                if (resource >= item.expiredTime) {//resource is current time large than expiredTime, so expired
                    expiredItemCallback(item)
                    this@AlarmClock.itemMap.remove(key)
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
        this.itemMap.clear()
        this.checkExpiredQueueThread.interrupt()
    }

    fun addItem(item: T) {
        if (this.itemMap.containsKey(item.key)) {
            error("key exists, key:%s".format(item.key))
        }
        this.itemMap[item.key] = item
    }

    fun updateItem(item: T): Boolean {
        return if (this.itemMap.containsKey(item.key)) {
            this.itemMap[item.key] = item
            true
        } else {
            false
        }
    }

    fun removeItem(key: String): T? {
        return this.itemMap.remove(key)
    }

    fun getItem(key: String): T? {
        return this.itemMap[key]
    }

    interface Item {
        val key: String
        val expiredTime: Long
    }
}