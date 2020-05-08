package com.oneliang.ktx.frame.jdbc

import java.sql.Connection

object TransactionManager {

    internal val customTransactionSign = ThreadLocal<Boolean>()
    internal val customTransactionConnection = ThreadLocal<Connection>()

    internal fun isCustomTransaction(): Boolean {
        var customTransaction = false
        val customTransactionSign = customTransactionSign.get()
        if (customTransactionSign != null && customTransactionSign) {
            customTransaction = true
        }
        return customTransaction
    }
}
