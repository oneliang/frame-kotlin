package com.oneliang.ktx.frame.test.rpc

import com.oneliang.ktx.frame.rpc.ProviderClient
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val providerClient = ProviderClient(HOST_ADDRESS, 9999)
    val provider = providerClient.lookup("server_a_cluster")
    println(provider)
    Thread.sleep(10000)
}