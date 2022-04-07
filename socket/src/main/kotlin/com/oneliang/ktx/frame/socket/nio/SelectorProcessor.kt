package com.oneliang.ktx.frame.socket.nio

interface SelectorProcessor {

    fun process(byteArray: ByteArray, socketChannelHashCode: Int): ByteArray

    fun notify(socketChannelHashCode: Int): ByteArray {
        return ByteArray(0)
    }
}