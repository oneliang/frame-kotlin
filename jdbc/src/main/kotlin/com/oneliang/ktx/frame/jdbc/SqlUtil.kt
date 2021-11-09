package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.json.toJson
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * sql util for generate the common sql such as:select,insert,delete,update
 * @author Dandelion
 * @since 2008-09-25
 */
object SqlUtil {

    @Throws(Throwable::class)
    internal fun fixTable(table: String, mappingBean: MappingBean?, sqlProcessor: SqlProcessor): String {
        return if (table.isBlank() && mappingBean == null) {
            throw MappingNotFoundException("Can not find the object mapping or table, object mapping or table can not be null or empty string!")
        } else {
            if (table.isBlank() && mappingBean != null) {
                val schema = mappingBean.schema
                if (schema.isBlank()) {
                    sqlProcessor.keywordSymbolLeft + mappingBean.table + sqlProcessor.keywordSymbolRight
                } else {
                    sqlProcessor.keywordSymbolLeft + schema + sqlProcessor.keywordSymbolRight + Constants.Symbol.DOT + sqlProcessor.keywordSymbolLeft + mappingBean.table + sqlProcessor.keywordSymbolRight
                }
            } else {//table is not blank, use input table first, don't add accent
                table
            }
        }
    }

    /**
     * Method: for database use,ResultSet to object list
     * @param <T>
     * @param resultSet
     * @param kClass
     * @param mappingBean
     * @return List<T>
    </T></T> */
    fun <T : Any> resultSetToObjectList(resultSet: ResultSet, kClass: KClass<T>, mappingBean: MappingBean, sqlProcessor: SqlProcessor): List<T> {
        val list = mutableListOf<T>()
        try {
            var instance: T?
            //			Field[] field = kClass.getDeclaredFields()// get the fields one
            val methods = kClass.java.methods
            // time is ok
            while (resultSet.next()) {
                instance = kClass.java.newInstance()// more instance
                for (method in methods) {
                    val methodName = method.name
                    val fieldName = if (methodName.startsWith(Constants.Method.PREFIX_SET)) {
                        ObjectUtil.methodNameToFieldName(Constants.Method.PREFIX_SET, methodName)
                    } else {
                        Constants.String.BLANK
                    }
                    if (fieldName.isBlank()) {
                        continue
                    }
                    val columnName = mappingBean.getColumn(fieldName)
                    if (columnName.isBlank()) {
                        continue
                    }
                    val classes = method.parameterTypes
                    var value: Any?
                    if (classes.size == 1) {
                        value = sqlProcessor.afterSelectProcess(classes[0].kotlin, resultSet, columnName)
                        method.invoke(instance, value)
                    }
                }
                list.add(instance)
            }
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return list
    }

    /**
     * Method: the simple select sql
     * @param selectColumns can be empty
     * @param table can not be null
     * @param condition can be blank
     * @param useDistinct default true
     * @return String
     */
    fun selectSql(selectColumns: Array<String> = emptyArray(), table: String, condition: String = Constants.String.BLANK, useDistinct: Boolean = true): String {
        val selectColumnStringBuilder = StringBuilder()
        if (selectColumns.isNotEmpty()) {
            selectColumnStringBuilder.append(selectColumns.joinToString(Constants.Symbol.COMMA))
        } else {
            selectColumnStringBuilder.append(Constants.Symbol.WILDCARD)
        }
        val sql = StringBuilder()
        sql.append("SELECT ")
        if (useDistinct) {
            sql.append("DISTINCT ")
        }
        sql.append(selectColumnStringBuilder)
        sql.append(" FROM ")
        sql.append(table)
        sql.append(" WHERE 1=1 ")
        sql.append(condition)
        return sql.toString()
    }

    /**
     * Method: class to select sql
     * @param <T>
     * @param selectColumns String[] which columns do you select
     * @param table
     * @param condition
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun selectSql(selectColumns: Array<String>, table: String, condition: String = Constants.String.BLANK, useDistinct: Boolean = true, mappingBean: MappingBean?, sqlProcessor: SqlProcessor): String {
        val tempTable = fixTable(table, mappingBean, sqlProcessor)
        return selectSql(selectColumns, tempTable, condition, useDistinct)
    }

    fun insertSql(table: String, columnNameArray: Array<String>, valueArray: Array<String>): String {
        return insertSql(table, columnNameArray.joinToString(), valueArray.joinToString(separator = Constants.Symbol.SINGLE_QUOTE + Constants.Symbol.COMMA + Constants.Symbol.SINGLE_QUOTE, prefix = Constants.Symbol.SINGLE_QUOTE, postfix = Constants.Symbol.SINGLE_QUOTE))
    }

    private fun insertSql(table: String, columnNames: String, values: String): String {
        val sql = StringBuilder()
        sql.append("INSERT INTO ")
        sql.append(table)
        sql.append("($columnNames)")
        sql.append(" VALUES ($values)")
        return sql.toString()
    }

    /**
     * update sql
     * @param table can not be null
     * @param columnsAndValueList
     * @param condition
     * @return String
     */
    fun updateSql(table: String, columnsAndValueList: List<String>, condition: String): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("UPDATE ")
        stringBuilder.append(table)
        stringBuilder.append(" SET " + columnsAndValueList.joinToString())
        stringBuilder.append(" WHERE 1=1 $condition")
        return stringBuilder.toString()
    }

    /**
     * delete sql
     * @param table can not be null
     * @param condition
     * @return String
     */
    fun deleteSql(table: String, condition: String = Constants.String.BLANK): String {
        val sql = StringBuilder()
        sql.append("DELETE FROM ")
        sql.append(table)
        sql.append(" WHERE 1=1 ")
        sql.append(condition)
        return sql.toString()
    }

    /**
     * delete sql
     * @param table
     * @param condition
     * @param mappingBean
     * @param sqlProcessor
     * @return String
     */
    fun deleteSql(table: String, condition: String = Constants.String.BLANK, mappingBean: MappingBean?, sqlProcessor: SqlProcessor): String {
        val tempTable = fixTable(table, mappingBean, sqlProcessor)
        return deleteSql(tempTable, condition)
    }

    /**
     * Method: class to select single id sql
     * @param <T>
     * @param kClass
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun <T : Any> classToSelectSingleIdSql(kClass: KClass<T>, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        return classToSelectIdSql(kClass, emptyArray(), mappingBean, sqlProcessor, SelectIdType.SINGLE_ID)
    }

    /**
     * Method: class to select multiple id sql, only for single id column
     * @param <T>
     * @param kClass
     * @param ids
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun <T : Any, IdType : Any> classToSelectMultipleIdSql(kClass: KClass<T>, ids: Array<IdType>, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        return classToSelectIdSql(kClass, ids, mappingBean, sqlProcessor, SelectIdType.MULTIPLE_ID)
    }

    /**
     * Method: class to select id sql, only for single id column
     * @param <T>
     * @param kClass
     * @param ids
     * @param mappingBean
     * @param sqlProcessor
     * @param selectIdType
     * @return String
    </T> */
    private fun <T : Any, IdType : Any> classToSelectIdSql(kClass: KClass<T>, ids: Array<IdType>, mappingBean: MappingBean, sqlProcessor: SqlProcessor, selectIdType: SelectIdType): String {
        val methods = kClass.java.methods
        val condition = StringBuilder()
        for (mappingColumnBean in mappingBean.mappingColumnBeanList) {
            val fieldName = mappingColumnBean.field
            if (fieldName.isBlank()) {
                continue
            }
            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            val isId = mappingBean.isId(fieldName)
            if (!isId) {
                continue
            }
            when (selectIdType) {
                SelectIdType.SINGLE_ID -> condition.append(" AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + " = ?")
                SelectIdType.MULTIPLE_ID -> {
                    if (ids.isEmpty()) {
                        throw NullPointerException("ids can not be null or empty.select id type:${selectIdType}")
                    }
                    val idsJson = ids.toJson()
                    val idsSql = idsJson.replace(("^\\" + Constants.Symbol.MIDDLE_BRACKET_LEFT).toRegex(), Constants.String.BLANK).replace(("\\" + Constants.Symbol.MIDDLE_BRACKET_RIGHT + "$").toRegex(), Constants.String.BLANK)
                    condition.append(" AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + " IN ($idsSql)")
                }
            }
        }
        val table = fixTable(Constants.String.BLANK, mappingBean, sqlProcessor)
        return selectSql(emptyArray(), table, condition.toString())
    }

    /**
     * Method: for database use make the delete sql string,can delete one row
     * not the many rows,support single id
     * @param <T>
     * @param kClass
     * @param id
     * @param mappingBean
     * @param sqlProcessor
     * @return String
     * @throws Exception
    </T> */
    @Throws(Exception::class)
    fun <T : Any, IdType : Any> classToDeleteSingleRowSql(kClass: KClass<T>, id: IdType, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        return classToDeleteSql(kClass, arrayOf<Any>(id), mappingBean, sqlProcessor, DeleteType.SINGLE_ROW)
    }

    /**
     * Method: for database use make the delete sql string,can delete multiple row
     * @param <T>
     * @param kClass
     * @param ids
     * @param mappingBean
     * @param sqlProcessor
     * @return String
     * @throws Exception
    </T> */
    @Throws(Exception::class)
    fun <T : Any, IdType : Any> classToDeleteMultipleRowSql(kClass: KClass<T>, ids: Array<IdType>, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        return classToDeleteSql(kClass, ids, mappingBean, sqlProcessor, DeleteType.MULTIPLE_ROW)
    }

    /**
     * Method: for database use make the delete sql string,can delete one row and multi row
     * @param <T>
     * @param kClass
     * @param ids
     * @param mappingBean
     * @param sqlProcessor
     * @param deleteType
     * @return String
     * @throws Exception
    </T> */
    @Throws(Exception::class)
    private fun <T : Any, IdType : Any> classToDeleteSql(kClass: KClass<T>, ids: Array<IdType>, mappingBean: MappingBean, sqlProcessor: SqlProcessor, deleteType: DeleteType): String {
        if (ids.isEmpty()) {
            throw NullPointerException("ids can not be null or empty.")
        }
        val condition = StringBuilder()
        for (mappingColumnBean in mappingBean.mappingColumnBeanList) {
            val fieldName = mappingColumnBean.field
            if (fieldName.isBlank()) {
                continue
            }
            val columnName = mappingBean.getColumn(fieldName)
            if (columnName.isBlank()) {
                continue
            }
            val isId = mappingBean.isId(fieldName)
            if (isId) {
                when (deleteType) {
                    DeleteType.SINGLE_ROW -> condition.append(" AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "='" + ids[0] + "'")
                    DeleteType.MULTIPLE_ROW -> {
                        val idsJson = ids.toJson()
                        val idsSql = idsJson.replace(("^\\" + Constants.Symbol.MIDDLE_BRACKET_LEFT).toRegex(), Constants.String.BLANK).replace(("\\" + Constants.Symbol.MIDDLE_BRACKET_RIGHT + "$").toRegex(), Constants.String.BLANK)
                        condition.append(" AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + " IN ($idsSql)")
                    }
                }
            }
        }
        val table = fixTable(Constants.String.BLANK, mappingBean, sqlProcessor)
        return deleteSql(table, condition.toString())
    }

    /**
     * Method: object to select sql
     * @param <T>
     * @param instance
     * @param table
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun <T : Any> objectToSelectSql(instance: T, table: String, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        val sql: String
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
                if (!isId) {
                    continue
                }
                val value = method.invoke(instance)
                condition.append(" AND " + sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + "='$value'")
            }
            val tempTable = fixTable(table, mappingBean, sqlProcessor)
            sql = selectSql(emptyArray(), tempTable, condition.toString())
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return sql
    }

    /**
     * Method: for database use,make the update sql string
     * @param <T>
     * @param instance
     * @param table
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun <T : Any> objectToInsertSql(instance: T, table: String, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        val sql: String
        try {
            val methods = instance.javaClass.methods
            val columnNames = StringBuilder()
            val values = StringBuilder()
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
                columnNames.append(sqlProcessor.keywordSymbolLeft + columnName + sqlProcessor.keywordSymbolRight + Constants.Symbol.COMMA)
                val type = method.returnType
                val value = method.invoke(instance)
//                if (sqlProcessor != null) {
                values.append(sqlProcessor.beforeInsertProcess(type.kotlin, value) + Constants.Symbol.COMMA)
            }
            val tempTable = fixTable(table, mappingBean, sqlProcessor)
            sql = insertSql(tempTable, columnNames.substring(0, columnNames.length - 1), values.substring(0, values.length - 1))
        } catch (e: Exception) {
            throw SqlUtilException(e)
        }
        return sql
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
     * @return String
    </T> */
    fun <T : Any> objectToUpdateSql(instance: T, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        val sql: String
        try {
            val methods = instance.javaClass.methods
            val columnsAndValueList = mutableListOf<String>()
            val condition = StringBuilder()
            for (method in methods) {
                val methodName = method.name
                val fieldName = ObjectUtil.methodNameToFieldName(methodName)
                if (fieldName.isBlank()) {
                    continue
                }
                val columnName = mappingBean.getColumn(fieldName)
                if (columnName.isBlank()) {//this field can not find the mapping
                    continue
                }
                val isId = mappingBean.isId(fieldName)
                val type = method.returnType
                val value = method.invoke(instance)
                val result = sqlProcessor.beforeUpdateProcess(type.kotlin, isId, columnName, value)
                if (isId) {
                    if (byId) {
                        condition.append(result)
                    }
                } else {
                    if (result.isNotBlank()) {
                        columnsAndValueList += result
                    }
                }
            }
            val tempTable = fixTable(table, mappingBean, sqlProcessor)
            sql = updateSql(tempTable, columnsAndValueList, "$condition $otherCondition")
        } catch (e: Throwable) {
            throw SqlUtilException(e)
        }
        return sql
    }

    /**
     * Method: for database use make the delete sql string,can delete one row
     * not the many rows
     * @param <T>
     * @param instance
     * @param table
     * @param otherCondition
     * @param mappingBean
     * @param sqlProcessor
     * @return String
    </T> */
    fun <T : Any> objectToDeleteSql(instance: T, table: String, otherCondition: String, byId: Boolean, mappingBean: MappingBean, sqlProcessor: SqlProcessor): String {
        val sql: String
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
                if (byId && isId || !byId && !isId) {
                    val type = method.returnType
                    val value = method.invoke(instance)
                    val result = sqlProcessor.beforeDeleteProcess(type.kotlin, isId, columnName, value)
                    condition.append(result)
                }
            }
            val tempTable = fixTable(table, mappingBean, sqlProcessor)
            sql = deleteSql(tempTable, "$condition $otherCondition")
        } catch (e: Throwable) {
            throw SqlUtilException(e)
        }
        return sql
    }

    /**
     * create table sqls,include drop table
     * @param annotationMappingBean
     * @param sqlProcessor
     * @return String[]
     */
    fun createTableSqls(annotationMappingBean: AnnotationMappingBean, sqlProcessor: SqlProcessor): Array<String> {
        val sqlList = mutableListOf<String>()
        val table = fixTable(Constants.String.BLANK, annotationMappingBean, sqlProcessor)
        if (annotationMappingBean.isDropIfExist) {
            sqlList.add("DROP TABLE IF EXISTS " + table + Constants.Symbol.SEMICOLON)
        }
        val createTableSql = StringBuilder()
        createTableSql.append("CREATE TABLE $table (")
        val mappingColumnBeanList = annotationMappingBean.mappingColumnBeanList
        if (mappingColumnBeanList.isNotEmpty()) {
            for (mappingColumnBean in mappingColumnBeanList) {
                if (mappingColumnBean is AnnotationMappingColumnBean) {
                    createTableSql.append(sqlProcessor.keywordSymbolLeft + mappingColumnBean.column + sqlProcessor.keywordSymbolRight)
                    createTableSql.append(" " + mappingColumnBean.condition + Constants.Symbol.COMMA)
                    if (mappingColumnBean.isId) {
                        createTableSql.append("PRIMARY KEY (" + mappingColumnBean.column + ")")
                        createTableSql.append(Constants.Symbol.COMMA)
                    }
                }
            }
            createTableSql.delete(createTableSql.length - 1, createTableSql.length)
        }
        createTableSql.append(") " + annotationMappingBean.condition!!)
        createTableSql.append(Constants.Symbol.SEMICOLON)
        sqlList.add(createTableSql.toString())
        return sqlList.toTypedArray()
    }

    class SqlUtilException(cause: Throwable) : RuntimeException(cause)

    interface SqlProcessor {

        val keywordSymbolLeft: String

        val keywordSymbolRight: String

        /**
         * statement process,for statement use
         * @param preparedStatement
         * @param parameter
         */
        fun statementProcess(preparedStatement: PreparedStatement, index: Int, parameter: Any?)

        /**
         * after select process,for result set to object
         * @param parameterType
         * @param resultSet
         * @param columnName
         * @return Object
         */
        fun afterSelectProcess(parameterType: KClass<*>, resultSet: ResultSet, columnName: String): Any?

        /**
         * before insert process,for generate insert sql
         * @param <T>
         * @param kClass
         * @param value
         * @return String
        </T> */
        fun <T : Any> beforeInsertProcess(kClass: KClass<T>, value: Any?): String

        /**
         * before update process,for generate update sql
         * @param <T>
         * @param kClass
         * @param isId
         * @param columnName
         * @param value
         * @return String
        </T> */
        fun <T : Any> beforeUpdateProcess(kClass: KClass<T>, isId: Boolean, columnName: String, value: Any?): String

        /**
         * before delete process,for generate delete sql
         * @param <T>
         * @param kClass
         * @param isId
         * @param columnName
         * @param value
         * @return String
        </T> */
        fun <T : Any> beforeDeleteProcess(kClass: KClass<T>, isId: Boolean, columnName: String, value: Any?): String
    }

    private enum class DeleteType {
        SINGLE_ROW, MULTIPLE_ROW
    }

    private enum class SelectIdType {
        SINGLE_ID, MULTIPLE_ID
    }
}