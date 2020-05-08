package com.oneliang.ktx.frame.jdbc

import java.sql.ResultSet

inline fun <R> ResultSet.use(block: ((ResultSet) -> R)): R {
    try {
        return block(this)
    } finally {
        this.statement.close()
        this.close()
    }
}