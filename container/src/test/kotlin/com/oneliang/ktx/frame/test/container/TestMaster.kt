package com.oneliang.ktx.frame.test.container

import com.oneliang.ktx.frame.container.Master
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val master = Master(9999)
    master.localTest = true
    master.jarFullFilename = "D:/master.jar"
    master.containerRunnableClassName = "com.oneliang.ktx.frame.test.container.TestMasterContainerRunner"
    master.start()
}