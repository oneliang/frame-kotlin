package com.oneliang.ktx.frame.test.locker

import com.oneliang.ktx.frame.locker.LockerClientManager
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val lockerClientManager = LockerClientManager("localhost", 9999)
    lockerClientManager.tryLock("lockKey")
    println("after try lock")
    Thread.sleep(1000)
    lockerClientManager.releaseLock("lockKey")
    println("after release lock")
}