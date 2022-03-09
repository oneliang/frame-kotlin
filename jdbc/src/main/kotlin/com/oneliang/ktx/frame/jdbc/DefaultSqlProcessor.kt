package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.generateZeroString
import com.oneliang.ktx.util.common.toFormatString
import java.util.*
import kotlin.reflect.KClass


/**
 * mostly for mysql database
 * @author Dandelion
 * @since 2011-01-07
 */
open class DefaultSqlProcessor : AbstractSqlProcessor() {

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

    /**
     * create table column process
     * @param column
     * @param type
     * @param idFlag
     * @param length
     * @param precision
     * @param nullable
     * @param defaultValue
     * @param comment
     * @return String
     */
    override fun createTableColumnDefinitionProcess(column: String, type: SqlUtil.ColumnType, idFlag: Boolean, length: Int, precision: Int, nullable: Boolean, defaultValue: String?, comment: String): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(this.keywordSymbolLeft + column + this.keywordSymbolRight)
        stringBuilder.append(Constants.String.SPACE)
        var optimizeDefaultValue = defaultValue
        stringBuilder.append(
            when (type) {
                SqlUtil.ColumnType.STRING -> {
                    optimizeDefaultValue = Constants.String.BLANK
                    if (length <= 0) {//custom default value
                        "TEXT"
                    } else {
                        "VARCHAR($length)"
                    }
                }
                SqlUtil.ColumnType.INT -> {
                    optimizeDefaultValue = Constants.String.ZERO
                    if (length <= 0) {//mysql default value
                        "INT(11)"
                    } else {
                        "INT($length)"
                    }
                }
                SqlUtil.ColumnType.LONG -> {
                    optimizeDefaultValue = Constants.String.ZERO
                    if (length <= 0) {
                        error("length:$length error, column:$column, column type:$type")
                    }
                    "BIGINT($length)"
                }
                SqlUtil.ColumnType.FLOAT -> {
                    if (length <= 0 || precision <= 0) {//mysql default value
                        optimizeDefaultValue = "0.0"
                        "FLOAT"
                    } else {
                        optimizeDefaultValue = "0." + generateZeroString(precision)
                        "FLOAT($length, $precision)"
                    }
                }
                SqlUtil.ColumnType.DOUBLE -> {
                    optimizeDefaultValue = "0.0"
                    if (length <= 0 || precision <= 0) {//mysql default value
                        "DOUBLE"
                    } else {
                        "DOUBLE($length, $precision)"
                    }
                }
                SqlUtil.ColumnType.DATE -> {
                    optimizeDefaultValue = Constants.Date.DEFAULT.toFormatString()
                    "DATETIME"
                }
                SqlUtil.ColumnType.BIG_DECIMAL -> {
                    if (length <= 0) {//mysql default value
                        optimizeDefaultValue = "0." + generateZeroString(2)
                        "DECIMAL(10, 2)"
                    } else {
                        optimizeDefaultValue = "0." + generateZeroString(precision)
                        "DECIMAL($length, $precision)"
                    }
                }
            }
        )
        if (!nullable) {
            stringBuilder.append(Constants.String.SPACE)
            stringBuilder.append("NOT NULL")
        }
        if (idFlag && type == SqlUtil.ColumnType.INT) {
            stringBuilder.append(Constants.String.SPACE)
            stringBuilder.append("AUTO_INCREMENT")
        } else {//not id add default value
            if (defaultValue != null) {
                stringBuilder.append(Constants.String.SPACE)
                stringBuilder.append("DEFAULT ${Constants.Symbol.SINGLE_QUOTE}${defaultValue.ifBlank { optimizeDefaultValue }}${Constants.Symbol.SINGLE_QUOTE}")
            }
        }
        if (comment.isNotBlank()) {
            stringBuilder.append(Constants.String.SPACE)
            stringBuilder.append("COMMENT ${Constants.Symbol.SINGLE_QUOTE}$comment${Constants.Symbol.SINGLE_QUOTE}")
        }
        return stringBuilder.toString()
    }

    /**
     * create table index process
     * @param primary
     * @param columns
     * @param command
     * @return String
     */
    override fun createTableIndexProcess(primary: Boolean, columns: Array<String>, command: String): String {
        val stringBuilder = StringBuilder()
        if (primary) {
            val columnsString = columns.joinToString { Constants.Symbol.ACCENT + it + Constants.Symbol.ACCENT }
            stringBuilder.append("PRIMARY KEY($columnsString)${command.ifBlank { " USING BTREE" }}")
        } else {
            val indexName = columns.joinToString(Constants.Symbol.UNDERLINE) { it.uppercase() } + "_INDEX"
            val columnsString = columns.joinToString { Constants.Symbol.ACCENT + it + Constants.Symbol.ACCENT }
            stringBuilder.append("KEY $indexName($columnsString)")
        }
        return stringBuilder.toString()
    }
}
