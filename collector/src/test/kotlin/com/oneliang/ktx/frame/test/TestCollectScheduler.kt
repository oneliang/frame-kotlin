package com.oneliang.ktx.frame.test

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.cache.FileCacheManager
import com.oneliang.ktx.frame.scheduler.Scheduler
import com.oneliang.ktx.util.logging.*
import java.io.File
import java.util.*

class TestCollectScheduler {
}

fun main() {
    val logRealPath = "D:/log"
    val defaultLogger = FileLogger(Logger.Level.VERBOSE, File(logRealPath), "default.log")
    val consoleLogger = BaseLogger(Logger.Level.VERBOSE)
    val loggerList = listOf(defaultLogger, consoleLogger)
    val complexLogger = ComplexLogger(Logger.Level.VERBOSE, loggerList, true)
    LoggerManager.registerLogger(Constants.Symbol.WILDCARD, complexLogger)
    complexLogger.info("log file:%s", defaultLogger.currentFileAbsolutePath)
    val scheduler = Scheduler(1, 2)
    val cacheDirectory = "/D:/bxg_com_cache"
    val fileCacheManager = FileCacheManager(cacheDirectory)
    scheduler.start()
    scheduler.addTimerTask(Date(), 60000L) {
//        BxgComCollectJob(fileCacheManager).collect(it)
    }
}