package com.oneliang.ktx.frame.broadcast

interface BroadcastReceiver {

    /**
     * receive
     * @param action
     * @param message
     */
    fun receive(action: String, message: Message)
}
