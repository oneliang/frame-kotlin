package com.oneliang.ktx.frame.test.transaction

import com.oneliang.ktx.frame.transaction.TransactionServer
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val transactionServer = TransactionServer("localhost", 9999)
    transactionServer.start()
    println("----------------------------------start-------------------------------")
}