package com.oneliang.ktx.frame.container

interface Communicable {

    /**
     * only for slave data tlv package
     */
    fun sendData(byteArray: ByteArray)

    fun receive()
}