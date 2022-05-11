package com.oneliang.ktx.frame.test.rpc

import com.oneliang.ktx.frame.rpc.RemoteProcessCallRequest
import com.oneliang.ktx.frame.rpc.fromByteArray
import com.oneliang.ktx.util.common.toByteArray
import com.oneliang.ktx.util.common.toHexString

fun main() {
    val remoteProcessCallRequest = RemoteProcessCallRequest()
    remoteProcessCallRequest.method = "method_a"
    remoteProcessCallRequest.parameters = arrayOf(1.toByteArray(), 2.toByteArray(), "aaa".toByteArray())
    val requestByteArray = remoteProcessCallRequest.toByteArray()
    println(requestByteArray.toHexString())
    val request = RemoteProcessCallRequest.fromByteArray(requestByteArray)
    println(request.toByteArray().toHexString())
}