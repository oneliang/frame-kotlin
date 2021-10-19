package com.oneliang.ktx.frame.test.socket

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toHexString
import java.net.DatagramPacket
import java.net.DatagramSocket

fun main() {
    val datagramSocket = DatagramSocket(9999)
    var count = 0
    while (count < 10) {
        val byteArray = ByteArray(Constants.Capacity.BYTES_PER_KB)
        val datagramPacket = DatagramPacket(byteArray, byteArray.size)
        datagramSocket.receive(datagramPacket)
        val totalLength = datagramPacket.length
        val receiveData = datagramPacket.data
        var data = ByteArray(0)
        println("total length:$totalLength, size: ${receiveData.size}, ${receiveData.toHexString()}")
        when {
            receiveData.size > totalLength -> {
                data = ByteArray(totalLength)
                receiveData.copyInto(data, 0, 0, totalLength)
            }
            receiveData.size == totalLength -> {
                data = receiveData
            }
            else -> {
            }
        }
        println(datagramPacket.address.toString() + "," + String(data, Charsets.UTF_8))
        count++
    }
}