package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.util.common.KotlinClassUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*
import kotlin.reflect.KClass

abstract class AbstractSqlProcessor : SqlUtil.SqlProcessor {

    companion object {
        private val logger = LoggerManager.getLogger(AbstractSqlProcessor::class)
    }

    override fun statementProcess(preparedStatement: PreparedStatement, index: Int, parameter: Any?) {
        try {
            if (parameter != null) {
                val parameterClass = parameter.javaClass
                val classType = KotlinClassUtil.getClassType(parameterClass.kotlin)
                if (classType != null) {
                    val value = parameter.toString()
                    when (classType) {
                        KotlinClassUtil.ClassType.KOTLIN_STRING, KotlinClassUtil.ClassType.KOTLIN_CHARACTER -> preparedStatement.setString(index, value)
                        KotlinClassUtil.ClassType.KOTLIN_BYTE -> preparedStatement.setByte(index, java.lang.Byte.parseByte(value))
                        KotlinClassUtil.ClassType.KOTLIN_SHORT -> preparedStatement.setShort(index, java.lang.Short.parseShort(value))
                        KotlinClassUtil.ClassType.KOTLIN_INTEGER -> preparedStatement.setInt(index, Integer.parseInt(value))
                        KotlinClassUtil.ClassType.KOTLIN_LONG -> preparedStatement.setLong(index, java.lang.Long.parseLong(value))
                        KotlinClassUtil.ClassType.KOTLIN_FLOAT -> preparedStatement.setFloat(index, java.lang.Float.parseFloat(value))
                        KotlinClassUtil.ClassType.KOTLIN_DOUBLE -> preparedStatement.setDouble(index, java.lang.Double.parseDouble(value))
                        KotlinClassUtil.ClassType.KOTLIN_BOOLEAN -> preparedStatement.setBoolean(index, java.lang.Boolean.parseBoolean(value))
                        KotlinClassUtil.ClassType.JAVA_UTIL_DATE -> preparedStatement.setTimestamp(index, Timestamp((parameter as Date).time))
                        else -> preparedStatement.setObject(index, parameter)
                    }
                } else {
                    preparedStatement.setObject(index, parameter)
                }
            } else {
                preparedStatement.setObject(index, null)
            }
        } catch (e: Throwable) {
            throw Exception(e)
        }
    }

    /**
     * after select process,for result set to object
     * @param parameterType
     * @param resultSet
     * @param columnName
     * @return Object
     */
    override fun afterSelectProcess(parameterType: KClass<*>, resultSet: ResultSet, columnName: String): Any? {
        var value: Any? = null
        try {
            val classType = KotlinClassUtil.getClassType(parameterType)
            when (classType) {
                KotlinClassUtil.ClassType.KOTLIN_CHARACTER -> value = resultSet.getString(columnName).toCharArray()[0]
                KotlinClassUtil.ClassType.KOTLIN_STRING -> value = resultSet.getString(columnName)
                KotlinClassUtil.ClassType.KOTLIN_BYTE -> value = java.lang.Byte.valueOf(resultSet.getByte(columnName))
                KotlinClassUtil.ClassType.KOTLIN_SHORT -> value = java.lang.Short.valueOf(resultSet.getShort(columnName))
                KotlinClassUtil.ClassType.KOTLIN_INTEGER -> value = Integer.valueOf(resultSet.getInt(columnName))
                KotlinClassUtil.ClassType.KOTLIN_LONG -> value = java.lang.Long.valueOf(resultSet.getLong(columnName))
                KotlinClassUtil.ClassType.KOTLIN_FLOAT -> value = java.lang.Float.valueOf(resultSet.getFloat(columnName))
                KotlinClassUtil.ClassType.KOTLIN_DOUBLE -> value = java.lang.Double.valueOf(resultSet.getDouble(columnName))
                KotlinClassUtil.ClassType.KOTLIN_BOOLEAN -> value = java.lang.Boolean.valueOf(resultSet.getBoolean(columnName))
                KotlinClassUtil.ClassType.JAVA_UTIL_DATE -> {
                    value = resultSet.getTimestamp(columnName)
                    if (value != null) {
                        value = Date(value.time)
                    }
                }
                else -> {
                    logger.error("unsupport class type:$parameterType")
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return value
    }
}
