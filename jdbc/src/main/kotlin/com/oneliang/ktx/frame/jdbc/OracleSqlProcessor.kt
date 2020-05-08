package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFormatString
import java.util.*
import kotlin.reflect.KClass


class OracleSqlProcessor : AbstractSqlProcessor() {

    /**
     * before insert process,for generate insert sql
     * @param <T>
     * @param kClass
     * @param value
     * @return String
    </T> */
    override fun <T : Any> beforeInsertProcess(kClass: KClass<T>, value: Any?): String {
        return if (kClass == Date::class) {
            if (value != null) {
                "to_date('" + (value as Date).toFormatString() + "','yyyy-mm-dd hh24:mi:ss')"
            } else {
                Constants.String.NULL
            }
        } else {
            if (value != null) {
                "'" + value.toString() + "'"
            } else {
                Constants.String.NULL
            }
        }
    }

    /**
     * before update process,for generate update sql
     * @param <T>
     * @param kClass
     * @param isId
     * @param columnName
     * @param value
     * @return String
    </T> */
    override fun <T : Any> beforeUpdateProcess(kClass: KClass<T>, isId: Boolean, columnName: String, value: Any?): String {
        val result: String
        if (isId) {
            result = "AND $columnName='$value'"
        } else {
            result = if (kClass == java.util.Date::class) {
                if (value != null) {
                    columnName + "=to_date('" + (value as java.util.Date).toFormatString() + "','yyyy-mm-dd hh24:mi:ss'),"
                } else {
                    Constants.String.BLANK
                }
            } else {
                if (value != null) {
                    "$columnName='$value',"
                } else {
                    Constants.String.BLANK
                }
            }
        }
        return result
    }

    /**
     * before delete process,for generate delete sql
     * @param <T>
     * @param kClass
     * @param isId
     * @param columnName
     * @param value
     * @return String
    </T> */
    override fun <T : Any> beforeDeleteProcess(kClass: KClass<T>, isId: Boolean, columnName: String, value: Any?): String {
        val result: String
        if (isId) {
            result = "AND $columnName='$value'"
        } else {
            result = if (kClass == java.util.Date::class) {
                if (value != null) {
                    " AND " + columnName + "=to_date('" + (value as java.util.Date).toFormatString() + "','yyyy-mm-dd hh24:mi:ss')"
                } else {
                    Constants.String.BLANK
                }
            } else {
                if (value != null) {
                    " AND $columnName='$value'"
                } else {
                    Constants.String.BLANK
                }
            }
        }
        return result
    }
}
