package com.oneliang.ktx.frame.transaction

import com.oneliang.ktx.util.common.toByteArray

object ConstantsTransaction {
    object Action {
        const val BEGIN_TRANSACTION = "beginTransaction"
        const val END_TRANSACTION = "endTransaction"
        const val ROLLBACK_TRANSACTION = "rollbackTransaction"
        const val EXECUTE_IN_TRANSACTION = "executeInTransaction"
    }

    object TlvPackageType {
        val BEGIN = 1.toByteArray()
        val END = 2.toByteArray()
        val ROLLBACK = 3.toByteArray()
        val EXECUTE_IN = 4.toByteArray()
    }
}