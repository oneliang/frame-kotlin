package com.oneliang.ktx.frame.test.rpc

import com.oneliang.ktx.frame.rpc.RemoteProcessCallProvider
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val remoteProcessCallProvider = RemoteProcessCallProvider(HOST_ADDRESS, 9999, "server_a_cluster", HOST_ADDRESS, 12000, object : RemoteProcessCallProvider.Processor {
        override fun process(method: String, parameters: Array<ByteArray>): ByteArray {
            println("method:%s, parameter size:%s".format(method, parameters.size))
            return ByteArray(0)
        }
    })
}