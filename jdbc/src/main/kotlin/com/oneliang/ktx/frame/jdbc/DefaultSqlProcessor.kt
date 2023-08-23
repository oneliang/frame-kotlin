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
     * @param fieldColumnMappingType
     * @param idFlag
     * @param length
     * @param precision
     * @param nullable
     * @param defaultValue for column, maybe be null
     * @param comment
     * @return String
     */
    override fun createTableColumnDefinitionProcess(
        column: String,
        fieldColumnMappingType: SqlUtil.FieldColumnMappingType,
        idFlag: Boolean,
        length: Int,
        precision: Int,
        nullable: Boolean,
        defaultValue: String?,
        comment: String
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(this.keywordSymbolLeft + column + this.keywordSymbolRight)
        stringBuilder.append(Constants.String.SPACE)
        var fixDefaultValue = defaultValue// use default value when not null
        stringBuilder.append(
            when (fieldColumnMappingType) {
                SqlUtil.FieldColumnMappingType.STRING -> {
                    if (length <= 0) {//can not set default value when not null
                        "TEXT"
                    } else {//custom default value
                        fixDefaultValue = Constants.String.BLANK
                        "VARCHAR($length)"
                    }
                }

                SqlUtil.FieldColumnMappingType.BOOLEAN -> {
                    fixDefaultValue = Constants.String.ZERO
                    "INT(1)"
                }

                SqlUtil.FieldColumnMappingType.INT -> {
                    fixDefaultValue = Constants.String.ZERO
                    if (length <= 0) {//mysql default value
                        "INT(11)"
                    } else {
                        "INT($length)"
                    }
                }

                SqlUtil.FieldColumnMappingType.LONG -> {
                    fixDefaultValue = Constants.String.ZERO
                    if (length <= 0) {
                        error("length:$length error, column:$column, column type:$fieldColumnMappingType")
                    }
                    "BIGINT($length)"
                }

                SqlUtil.FieldColumnMappingType.FLOAT -> {
                    if (length <= 0 || precision <= 0) {//mysql default value
                        fixDefaultValue = "0.0"
                        "FLOAT"
                    } else {
                        fixDefaultValue = "0." + generateZeroString(precision)
                        "FLOAT($length, $precision)"
                    }
                }

                SqlUtil.FieldColumnMappingType.DOUBLE -> {
                    fixDefaultValue = "0.0"
                    if (length <= 0 || precision <= 0) {//mysql default value
                        "DOUBLE"
                    } else {
                        "DOUBLE($length, $precision)"
                    }
                }

                SqlUtil.FieldColumnMappingType.DATE -> {
                    fixDefaultValue = Constants.Date.DEFAULT.toFormatString(Constants.Time.YEAR_MONTH_DAY)
                    "DATE"
                }

                SqlUtil.FieldColumnMappingType.DATETIME -> {
                    fixDefaultValue = Constants.Date.DEFAULT.toFormatString()
                    "DATETIME"
                }

                SqlUtil.FieldColumnMappingType.BIG_DECIMAL -> {
                    if (length <= 0) {//mysql default value
                        fixDefaultValue = "0." + generateZeroString(2)
                        "DECIMAL(10, 2)"
                    } else {
                        fixDefaultValue = "0." + generateZeroString(precision)
                        "DECIMAL($length, $precision)"
                    }
                }
            }
        )
        if (!nullable) {
            stringBuilder.append(Constants.String.SPACE)
            stringBuilder.append("NOT NULL")
        }
        if (idFlag && fieldColumnMappingType == SqlUtil.FieldColumnMappingType.INT) {
            stringBuilder.append(Constants.String.SPACE)
            stringBuilder.append("AUTO_INCREMENT")
        } else {//not id add default value, not null maybe need default value
            if (!nullable && fixDefaultValue != null) {
                stringBuilder.append(Constants.String.SPACE)
                stringBuilder.append("DEFAULT ${Constants.Symbol.SINGLE_QUOTE}${fixDefaultValue}${Constants.Symbol.SINGLE_QUOTE}")
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
