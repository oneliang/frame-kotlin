package com.oneliang.ktx.frame.socket.nio

interface SelectorProcessor {

    fun process(byteArray: ByteArray): ByteArray
}