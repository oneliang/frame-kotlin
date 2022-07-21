package com.oneliang.ktx.frame.test.container

import com.oneliang.ktx.frame.container.Slave
import com.oneliang.ktx.util.common.HOST_ADDRESS
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val sla = Slave(HOST_ADDRESS, 9999)
    sla.jarFullFilename = "D:/slave.jar"
    sla.containerRunnableClassName = "com.oneliang.ktx.frame.test.container.TestSlaveContainerRunner"
    sla.start()
}