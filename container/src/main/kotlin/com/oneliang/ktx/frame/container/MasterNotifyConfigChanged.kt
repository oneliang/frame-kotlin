package com.oneliang.ktx.frame.container

import com.oneliang.ktx.util.common.readWithBuffer
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toInt
import com.oneliang.ktx.util.packet.TlvPacket
import com.oneliang.ktx.util.packet.TlvPacketProcessor
import java.io.ByteArrayInputStream

internal class MasterNotifyConfigChanged : BaseData() {
    companion object {
        internal val tlvPacketProcessor = TlvPacketProcessor()
    }

    var data = ByteArray(0)

    fun toByteArray(): ByteArray {
        val tlvPacketList = mutableListOf<TlvPacket>()
        val idTlvPacket = TlvPacket(0.toByteArray(), this.id.toByteArray())
        tlvPacketList += idTlvPacket
        val actionTlvPacket = TlvPacket(0.toByteArray(), this.action.toByteArray())
        tlvPacketList += actionTlvPacket
        tlvPacketList += TlvPacket(0.toByteArray(), this.data)
        val subTlvPackets = tlvPacketList.toTypedArray()
        val tlvPacket = TlvPacket(subTlvPackets)
        return tlvPacket.toByteArray()
    }
}

internal fun MasterNotifyConfigChanged.Companion.build(id: String, data: ByteArray): MasterNotifyConfigChanged {
    val masterNotifyConfigChanged = MasterNotifyConfigChanged()
    masterNotifyConfigChanged.action = ConstantsContainer.Action.MASTER_NOTIFY_CONFIG_CHANGED
    masterNotifyConfigChanged.id = id
    masterNotifyConfigChanged.data = data
    return masterNotifyConfigChanged
}

internal fun MasterNotifyConfigChanged.Companion.fromByteArray(byteArray: ByteArray): MasterNotifyConfigChanged {
    val masterNotifyConfigChanged = MasterNotifyConfigChanged()
    val tlvPacket = tlvPacketProcessor.receiveTlvPacket(byteArray)
    val byteArrayInputStream = ByteArrayInputStream(tlvPacket.body)
    //id
    val idTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val idBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val idBodyLength = idBodyLengthByteArray.toInt()
    val idBody = byteArrayInputStream.readWithBuffer(idBodyLength)
    masterNotifyConfigChanged.id = String(idBody)
    //action
    val actionTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val actionBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val actionBodyLength = actionBodyLengthByteArray.toInt()
    val actionBody = byteArrayInputStream.readWithBuffer(actionBodyLength)
    masterNotifyConfigChanged.action = String(actionBody)
    //data
    val dataTypeByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.typeByteArrayLength)
    val dataBodyLengthByteArray = byteArrayInputStream.readWithBuffer(tlvPacketProcessor.bodyLengthByteArrayLength)
    val dataBodyLength = dataBodyLengthByteArray.toInt()
    val dataBody = byteArrayInputStream.readWithBuffer(dataBodyLength)
    masterNotifyConfigChanged.data = dataBody
    return masterNotifyConfigChanged
}