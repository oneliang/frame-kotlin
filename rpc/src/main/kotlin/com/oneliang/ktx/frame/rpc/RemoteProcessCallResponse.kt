package com.oneliang.ktx.frame.rpc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.readWithBuffer
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.io.ByteArrayInputStream

internal class RemoteProcessCallResponse {
    companion object

    var id = Constants.String.BLANK
    var method = Constants.String.BLANK
    var success = false
    var result = ByteArray(0)

    fun toByteArray(): ByteArray {
        val tlvPacketList = mutableListOf<TlvPacket>()
        val idTlvPacket = TlvPacket(0.toByteArray(), this.id.toByteArray())
        tlvPacketList += idTlvPacket
        val methodTlvPacket = TlvPacket(0.toByteArray(), this.method.toByteArray())
        tlvPacketList += methodTlvPacket
        val successTlvPacket = TlvPacket(0.toByteArray(), (if (this.success) 1 else 0).toByteArray())
        tlvPacketList += successTlvPacket
        val resultTlvPacket = TlvPacket(0.toByteArray(), result)
        tlvPacketList += resultTlvPacket
        val subTlvPackets = tlvPacketList.toTypedArray()
        val tlvPacket = TlvPacket(subTlvPackets)
        return tlvPacket.toByteArray()
    }
}

internal fun RemoteProcessCallResponse.Companion.fromByteArray(byteArray: ByteArray): RemoteProcessCallResponse {
    val remoteProcessCallResponse = RemoteProcessCallResponse()
    val tlvPacketProcessor = TlvPacketProcessor()
    val tlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
    val byteArrayInputStream = ByteArrayInputStream(tlvPacket.body)
    //id
    val idTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val idBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val idBodyLength = idBodyLengthByteArray.toInt()
    val idBody = byteArrayInputStream.readWithBuffer(idBodyLength)
    remoteProcessCallResponse.id = String(idBody)
    //method
    val methodTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val methodBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val methodBodyLength = methodBodyLengthByteArray.toInt()
    val methodBody = byteArrayInputStream.readWithBuffer(methodBodyLength)
    remoteProcessCallResponse.method = String(methodBody)
    //success
    val successTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val successBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val successBodyLength = successBodyLengthByteArray.toInt()
    val successBody = byteArrayInputStream.readWithBuffer(successBodyLength)
    remoteProcessCallResponse.success = successBody.toInt() > 0
    //result
    val resultTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val resultBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val resultBodyLength = resultBodyLengthByteArray.toInt()
    val resultBody = byteArrayInputStream.readWithBuffer(resultBodyLength)
    remoteProcessCallResponse.result = resultBody
    return remoteProcessCallResponse
}

internal fun RemoteProcessCallResponse.Companion.build(id: String, method: String, success: Boolean, result: ByteArray): RemoteProcessCallResponse {
    val remoteProcessCallResponse = RemoteProcessCallResponse()
    remoteProcessCallResponse.id = id
    remoteProcessCallResponse.method = method
    remoteProcessCallResponse.success = success
    remoteProcessCallResponse.result = result
    return remoteProcessCallResponse
}