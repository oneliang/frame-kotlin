package com.oneliang.ktx.frame.test.container

import com.oneliang.ktx.frame.container.Slave
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val slave = Slave(HOST_ADDRESS, 9999)
    slave.localTest = true
    slave.jarFullFilename = "D:/slave.jar"
    slave.containerExecutorClassName = "com.oneliang.ktx.frame.test.container.TestSlaveContainerRunner"
    slave.start()
}