package com.oneliang.ktx.frame.socket.nio

import com.oneliang.ktx.Constants
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

fun SocketChannel.readByteArray(): ByteArray {
    val byteBuffer = ByteBuffer.allocate(Constants.Capacity.BYTES_PER_KB)
    val byteArrayOutputStream = ByteArrayOutputStream()
    var length: Int
    while (this.read(byteBuffer).also { length = it } != 0) {
        byteBuffer.flip()
        val byteArray = ByteArray(length)
        byteBuffer.get(byteArray, 0, byteArray.size)
        byteArrayOutputStream.write(byteArray)
        byteArrayOutputStream.flush()
        byteBuffer.clear()
    }
    val byteArray = byteArrayOutputStream.toByteArray()
    byteArrayOutputStream.close()
    return byteArray
}