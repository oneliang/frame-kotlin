package com.oneliang.ktx.frame.broadcast

import com.oneliang.ktx.util.concurrent.LoopThread
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

class BroadcastManager() : LoopThread(), BroadcastSender {
    companion object {
        private val logger = LoggerManager.getLogger(BroadcastManager::class)
    }

    private val broadcastReceiverMap: MutableMap<String, MutableList<BroadcastReceiver>> = ConcurrentHashMap()
    private val messageQueue: Queue<Message> = ConcurrentLinkedQueue()
    private val lock = Object()

    /**
     * register broadcast receiver
     * @param actionFilter
     * @param broadcastReceiver
     */
    fun registerBroadcastReceiver(actionFilter: Array<String>, broadcastReceiver: BroadcastReceiver) {
        if (actionFilter.isEmpty()) {
            return
        }
        for (actionKey in actionFilter) {
            if (actionKey.isBlank()) {
                continue
            }
            val broadcastReceiverList = this.broadcastReceiverMap.getOrPut(actionKey) { CopyOnWriteArrayList() }
            broadcastReceiverList.add(broadcastReceiver)
        }
    }

    /**
     * unregister broadcast receiver
     * @param broadcastReceiver
     */
    fun unregisterBroadcastReceiver(broadcastReceiver: BroadcastReceiver) {
        val iterator = broadcastReceiverMap.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val broadcastReceiverList = entry.value
            if (broadcastReceiverList.contains(broadcastReceiver)) {
                broadcastReceiverList.remove(broadcastReceiver)
            }
        }
    }

    /**
     * send broadcast message
     * @param message
     */
    override fun sendBroadcast(message: Message) {
        this.messageQueue.add(message)
        synchronized(lock) {
            lock.notify()
        }
    }

    override fun looping() {
        if (!this.messageQueue.isEmpty()) {
            val message = this.messageQueue.poll()
            handleMessage(message)
        } else {
            synchronized(lock) {
                lock.wait()
            }
        }
    }

    /**
     * handle message
     * @param message
     */
    private fun handleMessage(message: Message) {
        val actionList = message.actionList
        if (actionList.isEmpty()) {
            return
        }
        for (action in actionList) {
            if (!broadcastReceiverMap.containsKey(action)) {
                continue
            }
            val broadcastReceiverList = broadcastReceiverMap[action]
            if (broadcastReceiverList == null || broadcastReceiverList.isEmpty()) {
                continue
            }
            val classList = message.classList
            if (classList.isEmpty()) {
                for (broadcastReceiver in broadcastReceiverList) {
                    broadcastReceiver.receive(action, message)
                }
            } else {
                for (broadcastReceiver in broadcastReceiverList) {
                    if (classList.contains(broadcastReceiver::class)) {
                        broadcastReceiver.receive(action, message)
                    }
                }
            }
        }
    }
}
