package com.oneliang.ktx.frame.test.rpc

import com.oneliang.ktx.frame.rpc.RemoteProcessCallClient
import com.oneliang.ktx.frame.rpc.RemoteProcessCallResponse
import com.oneliang.ktx.frame.rpc.fromByteArray
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.packet.TlvPacketProcessor

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val tlvPacketProcessor = TlvPacketProcessor()
    val remoteProcessCallClient = RemoteProcessCallClient(HOST_ADDRESS, 9999) {
        val remoteProcessCallResponse = RemoteProcessCallResponse.fromByteArray(it)
        println("id:%s, method:%s, success:%s, result byte array size:%s".format(remoteProcessCallResponse.id, remoteProcessCallResponse.method, remoteProcessCallResponse.success, remoteProcessCallResponse.result.size))
    }
    val lookupSign = remoteProcessCallClient.lookup("server_a_cluster")
    println(lookupSign)
    if (lookupSign) {
        remoteProcessCallClient.remoteProcessCall("a", arrayOf(1.toByteArray()))
    } else {
    }
    Thread.sleep(10000)
}