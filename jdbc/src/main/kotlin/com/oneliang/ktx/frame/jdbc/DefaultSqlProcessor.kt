package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFormatString
import java.util.*
import kotlin.reflect.KClass


/**
 * mostly for mysql database
 * @author Dandelion
 * @since 2011-01-07
 */
class DefaultSqlProcessor : AbstractSqlProcessor() {

    override val keywordSymbolLeft: String = Constants.Symbol.ACCENT
    override val keywordSymbolRight: String = Constants.Symbol.ACCENT

    /**
     * mostly for mysql database
     * default sql insert processor
     * promise:if value is null return null,if it is not null return the new value
     */
    override fun <T : Any> beforeInsertProcess(kClass: KClass<T>, value: Any?): String {
        return if (value != null) {
            when (kClass) {
                Boolean::class -> value.toString()
                Date::class -> "'" + (value as Date).toFormatString() + "'"
                else -> "'$value'"
            }
        } else {
            Constants.String.NULL
        }
    }

    /**
     * mostly for mysql database
     * default sql update processor
     * promise:if value is null,return the blank,if it is not null return the new value
     */
    override fun <T : Any> beforeUpdateProcess(kClass: KClass<T>, isId: Boolean, columnName: String, value: Any?): String {
        return if (isId) {
            " AND " + this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "='$value'"
        } else {
            if (value != null) {
                when (kClass) {
                    Boolean::class -> this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "=$value"
                    Date::class -> this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "='" + (value as Date).toFormatString() + "'"
                    else -> this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "='$value'"
                }
            } else {
                Constants.String.BLANK
            }
        }
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
        return if (isId) {
            " AND " + this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "='$value'"
        } else {
            if (value != null) {
                when (kClass) {
                    Boolean::class -> " AND " + this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "=$value"
                    Date::class -> " AND " + this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "='" + (value as Date).toFormatString() + "'"
                    else -> " AND " + this.keywordSymbolLeft + columnName + this.keywordSymbolRight + "='$value'"
                }
            } else {
                Constants.String.BLANK
            }
        }
    }
}
