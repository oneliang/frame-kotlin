package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.ObjectUtil
import kotlin.reflect.KClass

/**
 * sql util for generate the common sql such as:select,insert,delete,update
 * @author Dandelion
 * @since 2008-09-25
 */
object SqlInjectUtil {

    /**
     * Method: for database use,make the insert sql stringparameterList
     * @param <T>
     * @param instance
     * @param table
     * @param mappingBean
     * @param sqlProcessor
     * @return Pair<String,List<Any>>
    </T> */
    fun <T : Any> objectToInsertSql(instance: T, table: String, mappingBean: MappingBean, sqlProcessor: SqlUtil.SqlProcessor): Pair<String, List<Any?>> {
        val sql = StringBuilder()
        val parameterList = mutableListOf<Any?>()
        try {
            val methods = instance.javaClass.methods
            val columnNameList = mutableListOf<String>()
            val valueList = mutableListOf<String>()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                columnNameList += sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight
                val value = method.invoke(instance)
                valueList += Constants.Symbol.QUESTION_MARK
                parameterList.add(value)
            }
            val fixTable = SqlUtil.fixTable(table, mappingBean, sqlProcessor)
            sql.append("INSERT INTO ")
            sql.append(fixTable)
            sql.append("(" + columnNameList.joinToString() + ")")
            sql.append(" VALUES (" + valueList.joinToString() + ")")
        } catch (e: Throwable) {
            throw SqlInjectUtilException(e)
        }
        return sql.toString() to parameterList
    }

    /**
     * Method: for database use,make the insert sql string
     * @param <T>
     * @param kClass
     * @param table
     * @param mappingBean
     * @param sqlProcessor
     * @return Pair<String, List<String>>
    </T> */
    fun <T : Any> classToInsertSql(kClass: KClass<T>, table: String, mappingBean: MappingBean, sqlProcessor: SqlUtil.SqlProcessor): Pair<String, List<String>> {
        val sql = StringBuilder()
        val fieldNameList = mutableListOf<String>()
        val methods = kClass.java.methods
        val columnNameList = mutableListOf<String>()
        val valueList = mutableListOf<String>()
        for (method in methods) {
            val methodName = method.name
            val fieldName = ObjectUtil.methodNameToFieldName(methodName)
            if (fieldName.isBlank()) {
                continue
            }
            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            columnNameList += sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight
            valueList += Constants.Symbol.QUESTION_MARK
            fieldNameList.add(fieldName)
        }
        val fixTable = SqlUtil.fixTable(table, mappingBean, sqlProcessor)
        sql.append("INSERT INTO ")
        sql.append(fixTable)
        sql.append("(" + columnNameList.joinToString() + ")")
        sql.append(" VALUES (" + valueList.joinToString() + ")")
        return sql.toString() to fieldNameList
    }

    /**
     * Method: for database use,make the update sql string
     * @param <T>
     * @param instance
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @param sqlProcessor
     * @return Pair<String, List<Any>>
    </T> */
    fun <T : Any> objectToUpdateSql(instance: T, updateFields: Array<String> = emptyArray(), table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlUtil.SqlProcessor): Pair<String, List<Any?>> {
        try {
            val idList = mutableListOf<Any>()
            val valueList = mutableListOf<Any>()
            val parameterList = mutableListOf<Any?>()
            val methods = instance.javaClass.methods
            val columnsAndValueList = mutableListOf<String>()
            val condition = StringBuilder()
            val updateFieldSet = updateFields.toHashSet()
            val allColumn = updateFieldSet.isEmpty()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                val isId = mappingBean.isId(fieldName)
                if (!allColumn && !updateFieldSet.contains(fieldName) && !isId) {
                    continue
                }
                val value = method.invoke(instance)
                if (byId && isId) {
                    val result: String
                    if (value != null) {
                        result = " AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "=?"
                        idList.add(value)
                        condition.append(result)
                    } else {
                        result = " AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + " is " + Constants.String.NULL
                        condition.append(result)
                    }
                } else {
                    if (value != null) {
                        val result = sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "=?"
                        valueList.add(value)
                        columnsAndValueList += result
                    }
                }
            }
            val fixTable = SqlUtil.fixTable(table, mappingBean, sqlProcessor)
            val sql = SqlUtil.updateSql(fixTable, columnsAndValueList, "$condition $otherCondition")
            parameterList.addAll(valueList)
            parameterList.addAll(idList)
            return sql to parameterList
        } catch (e: Exception) {
            throw SqlInjectUtilException(e)
        }
    }

    /**
     * Method: for database use,make the update sql string
     * @param <T>
     * @param kClass
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @param sqlProcessor
     * @return Pair<String, List<String>>
    </T> */
    fun <T : Any> classToUpdateSql(kClass: KClass<T>, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlUtil.SqlProcessor): Pair<String, List<String>> {
        val fieldNameList = mutableListOf<String>()
        val idList = ArrayList<String>()
        val valueList = ArrayList<String>()
        val methods = kClass.java.methods
        val columnsAndValueList = mutableListOf<String>()
        val condition = StringBuilder()
        for (method in methods) {
            val methodName = method.name
            val fieldName = ObjectUtil.methodNameToFieldName(methodName)
            if (fieldName.isBlank()) {
                continue
            }
            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            val isId = mappingBean.isId(fieldName)
            if (byId && isId) {
                val result = " AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "=?"
                idList.add(fieldName)
                condition.append(result)
            } else {
                val result = sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "=?"
                valueList.add(fieldName)
                columnsAndValueList += result
            }
        }
        val fixTable = SqlUtil.fixTable(table, mappingBean, sqlProcessor)
        val sql = SqlUtil.updateSql(fixTable, columnsAndValueList, "$condition $otherCondition")
        fieldNameList.addAll(valueList)
        fieldNameList.addAll(idList)
        return sql to fieldNameList
    }

    /**
     * Method: for database use make the delete sql string,can delete one row
     * not the many rows
     * @param <T>
     * @param instance
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @param sqlProcessor
     * @return Pair<String,List<Any>>
    </T> */
    fun <T : Any> objectToDeleteSql(instance: T, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlUtil.SqlProcessor): Pair<String, List<Any?>> {
        val sql: String
        val parameterList = mutableListOf<Any?>()
        try {
            val methods = instance.javaClass.methods
            val condition = StringBuilder()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {
                    continue
                }
                val isId = mappingBean.isId(fieldName)
                if ((byId && isId) || (!byId && !isId)) {
                    val value = method.invoke(instance)
                    if (value != null) {
                        condition.append(" AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "=?")
                        parameterList.add(value)
                    } else {//only delete not null value when delete not by id, because null will be a condition value
                        if (byId && isId) {
                            condition.append(" AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + " is " + Constants.String.NULL)
                        }
                    }
                }
            }
            val fixTable = SqlUtil.fixTable(table, mappingBean, sqlProcessor)
            sql = SqlUtil.deleteSql(fixTable, "$condition $otherCondition")
        } catch (e: Exception) {
            throw SqlInjectUtilException(e)
        }
        return sql to parameterList
    }

    /**
     * Method: for database use make the delete sql string,sql binding
     * @param <T>
     * @param kClass
     * @param table
     * @param otherCondition
     * @param byId
     * @param mappingBean
     * @param sqlProcessor
     * @return Pair<String,List<String>>
    </T> */
    fun <T : Any> classToDeleteSql(kClass: KClass<T>, table: String = Constants.String.BLANK, otherCondition: String = Constants.String.BLANK, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlUtil.SqlProcessor): Pair<String, List<String>> {
        val methods = kClass.java.methods
        val fieldNameList = mutableListOf<String>()
        val conditionList = mutableListOf<String>()
        for (method in methods) {
            val methodName = method.name
            val fieldName = ObjectUtil.methodNameToFieldName(methodName)
            if (fieldName.isBlank()) {
                continue
            }

            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            val isId = mappingBean.isId(fieldName)
            if ((byId && isId) || (!byId && !isId)) {
                conditionList += " AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "=?"
                fieldNameList.add(fieldName)
            }
        }
        val fixTable = SqlUtil.fixTable(table, mappingBean, sqlProcessor)
        val sql = SqlUtil.deleteSql(fixTable, "${conditionList.joinToString()} $otherCondition")
        return sql to fieldNameList
    }

    class SqlInjectUtilException(cause: Throwable) : RuntimeException(cause)
}
