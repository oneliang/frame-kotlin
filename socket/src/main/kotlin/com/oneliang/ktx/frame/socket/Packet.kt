package com.oneliang.ktx.frame.socket

interface Packet {

    @Throws(Exception::class)
    fun toByteArray(): ByteArray
}