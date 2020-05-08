package com.oneliang.ktx.frame.broadcast

interface BroadcastSender {

    /**
     * send broadcast message
     * @param message
     */
    fun sendBroadcast(message: Message)
}
