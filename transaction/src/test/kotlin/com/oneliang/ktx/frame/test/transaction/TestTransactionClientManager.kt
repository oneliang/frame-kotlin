package com.oneliang.ktx.frame.test.transaction

import com.oneliang.ktx.frame.transaction.TransactionClientManager
import com.oneliang.ktx.util.logging.BaseLogger
import com.oneliang.ktx.util.logging.Logger
import com.oneliang.ktx.util.logging.LoggerManager

fun main() {
    LoggerManager.registerLogger("*", BaseLogger(Logger.Level.DEBUG))
    val transactionClientManager = TransactionClientManager("localhost", 9999)
    transactionClientManager.executeTransaction {

        true
    }
    Thread.sleep(10000)
}