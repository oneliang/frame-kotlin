package com.oneliang.ktx.frame.container

interface Communicable {

    val id: String

    /**
     * only for slave data tlv package
     */
    fun sendData(byteArray: ByteArray)

    /**
     * set receive callback
     * @param receiveCallback
     */
    fun setReceiveCallback(receiveCallback: ReceiveCallback)

    interface ReceiveCallback {
        fun receive(byteArray: ByteArray)
    }
}