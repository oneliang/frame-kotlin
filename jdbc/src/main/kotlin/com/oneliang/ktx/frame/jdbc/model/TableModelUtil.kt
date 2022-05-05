package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jdbc.SqlUtil
import com.oneliang.ktx.util.common.toArray
import java.math.BigDecimal

object TableModelUtil {

    private fun fixSchemaTable(schema: String, table: String, sqlProcessor: SqlUtil.SqlProcessor): String {
        if (table.isBlank()) {
            error("table can not be blank, please input the table")
        }
        return if (schema.isBlank()) {
            sqlProcessor.keywordSymbolLeft + table + sqlProcessor.keywordSymbolRight
        } else {
            sqlProcessor.keywordSymbolLeft + schema + sqlProcessor.keywordSymbolRight + Constants.Symbol.DOT + sqlProcessor.keywordSymbolLeft + table + sqlProcessor.keywordSymbolRight
        }
    }

    fun dropTableSql(tableModel: TableModel, sqlProcessor: SqlUtil.SqlProcessor): String {
        val schemaTable = fixSchemaTable(tableModel.schema, tableModel.table, sqlProcessor)
        return "DROP TABLE IF EXISTS $schemaTable ${Constants.Symbol.SEMICOLON}"
    }

    fun createTableSql(tableModel: TableModel, sqlProcessor: SqlUtil.SqlProcessor): String {
        val schemaTable = fixSchemaTable(tableModel.schema, tableModel.table, sqlProcessor)
        val columnDefinitionSqlList = mutableListOf<String>()
        val columnIndexSqlList = mutableListOf<String>()
        tableModel.columnArray.forEach {
            columnDefinitionSqlList += sqlProcessor.createTableColumnDefinitionProcess(it.column, SqlUtil.FieldColumnMappingType.valueOf(it.type), it.idFlag, it.length, it.precision, it.nullable, it.columnDefaultValue, it.comment)
            if (it.idFlag) {
                columnIndexSqlList += sqlProcessor.createTableIndexProcess(it.idFlag, arrayOf(it.column), Constants.String.BLANK)
            }
        }
        tableModel.indexArray.forEach {
            val columnList = it.columns.split(Constants.Symbol.COMMA)
            val columnArray = columnList.toArray { column -> column.trim() }
            columnIndexSqlList += sqlProcessor.createTableIndexProcess(false, columnArray, it.otherCommands)
        }
        val columnDefinitionSql = (columnDefinitionSqlList + columnIndexSqlList).joinToString(Constants.Symbol.COMMA + Constants.String.NEW_LINE)
        return SqlUtil.createTableSql(schemaTable, columnDefinitionSql, Constants.String.BLANK)
    }
}

fun main() {
    println(BigDecimal::class.qualifiedName)
}