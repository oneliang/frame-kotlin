package com.oneliang.ktx.frame.test.locker

import com.oneliang.ktx.frame.locker.LockerServer
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val lockerServer = LockerServer("localhost", 9999)
    lockerServer.start()
    println("----------------------------------start-------------------------------")
}