package com.oneliang.ktx.frame.test.socket.lock

import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val lockerClient = LockerClient("localhost", 9999)
    lockerClient.tryLock("lockKey")
    println("after try lock")
    Thread.sleep(50000)
    lockerClient.releaseLock("lockKey")
    println("after release lock")
}