package com.oneliang.ktx.frame.socket

import com.oneliang.ktx.util.common.readWithBuffer
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.common.writeWithBuffer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class TlvPacketProcessor(private val typeByteArrayLength: Int = 4, private val bodyLengthByteArrayLength: Int = 4) {

    @Throws(Throwable::class)
    fun sendTlvPacket(outputStream: OutputStream, tlvPacket: TlvPacket) {
        send(outputStream, tlvPacket.toByteArray())
    }

    @Throws(Throwable::class)
    private fun send(outputStream: OutputStream, byteArray: ByteArray) {
        outputStream.writeWithBuffer(byteArray)
    }

    @Throws(Throwable::class)
    private fun receiveType(inputStream: InputStream): ByteArray {
        return inputStream.readWithBuffer(this.typeByteArrayLength)
    }

    @Throws(Throwable::class)
    private fun receiveBody(inputStream: InputStream): ByteArray {
        val bodyOutputStream = ByteArrayOutputStream()
        this.receiveBody(inputStream, bodyOutputStream)
        return bodyOutputStream.toByteArray()
    }

    @Throws(Throwable::class)
    private fun receiveBody(inputStream: InputStream, outputStream: OutputStream) {
        val bodyLengthByteArray = inputStream.readWithBuffer(this.bodyLengthByteArrayLength)
        val bodyLength: Int = bodyLengthByteArray.toInt()
        inputStream.readWithBuffer(bodyLength, outputStream)
    }

    @Throws(Throwable::class)
    fun receiveTlvPacket(inputStream: InputStream): TlvPacket {
        val type = receiveType(inputStream)
        val bodyByteArray = this.receiveBody(inputStream)
        return TlvPacket(type, bodyByteArray)
    }

    @Throws(Throwable::class)
    fun receiveTlvPacket(byteArray: ByteArray): TlvPacket {
        val inputStream = ByteArrayInputStream(byteArray)
        val type = receiveType(inputStream)
        val bodyByteArray = this.receiveBody(inputStream)
        return TlvPacket(type, bodyByteArray)
    }
}