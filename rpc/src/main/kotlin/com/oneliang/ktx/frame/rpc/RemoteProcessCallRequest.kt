package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.readWithBuffer
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.io.ByteArrayInputStream

internal class RemoteProcessCallRequest {
    companion object

    var id = Constants.String.BLANK
    var method = Constants.String.BLANK
    var parameters = emptyArray<ByteArray>()

    fun toByteArray(): ByteArray {
        val tlvPacketList = mutableListOf<TlvPacket>()
        val idTlvPacket = TlvPacket(0.toByteArray(), id.toByteArray())
        tlvPacketList += idTlvPacket
        val methodTlvPacket = TlvPacket(0.toByteArray(), method.toByteArray())
        tlvPacketList += methodTlvPacket
        parameters.forEachIndexed { index, byteArray ->
            tlvPacketList += TlvPacket(index.toByteArray(), byteArray)
        }
        val subTlvPackets = tlvPacketList.toTypedArray()
        val tlvPacket = TlvPacket(subTlvPackets)
        return tlvPacket.toByteArray()
    }
}

internal fun RemoteProcessCallRequest.Companion.fromByteArray(byteArray: ByteArray): RemoteProcessCallRequest {
    val remoteProcessCallRequest = RemoteProcessCallRequest()
    val tlvPacketProcessor = TlvPacketProcessor()
    val tlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
    val byteArrayInputStream = ByteArrayInputStream(tlvPacket.body)
    //id
    val idTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val idBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val idBodyLength = idBodyLengthByteArray.toInt()
    val idBody = byteArrayInputStream.readWithBuffer(idBodyLength)
    remoteProcessCallRequest.id = String(idBody)
    //method
    val methodTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val methodBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val methodBodyLength = methodBodyLengthByteArray.toInt()
    val methodBody = byteArrayInputStream.readWithBuffer(methodBodyLength)
    remoteProcessCallRequest.method = String(methodBody)
    val parameterList = mutableListOf<ByteArray>()
    while (byteArrayInputStream.available() > 0) {
        val typeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
        val parameterBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
        val parameterBodyLength = parameterBodyLengthByteArray.toInt()
        val parameterBody = byteArrayInputStream.readWithBuffer(parameterBodyLength)
        parameterList += parameterBody
    }
    remoteProcessCallRequest.parameters = parameterList.toTypedArray()
    return remoteProcessCallRequest
}