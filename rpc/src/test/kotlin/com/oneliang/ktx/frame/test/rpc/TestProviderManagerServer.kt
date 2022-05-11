package com.oneliang.ktx.frame.test.rpc

import com.oneliang.ktx.frame.rpc.ProviderManagerServer
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val providerManagerServer = ProviderManagerServer(port = 9999)
    providerManagerServer.start()
    println("----------------------------------start-------------------------------")
}