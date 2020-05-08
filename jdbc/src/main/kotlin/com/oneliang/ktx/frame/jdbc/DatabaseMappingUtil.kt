package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.util.common.parseRegexGroup

/**
 * for db mapping file use,through the class can find the table column which
 * field map
 *
 * @author Dandelion
 * @since 2008-12-29
 */
object DatabaseMappingUtil {

    /**
     * regex
     */
    private const val REGEX = "\\{([\\w\\.]*)\\}"

    /**
     * parse sql like:select * from {User}--can find the mapping file where {User.id} and so on
     * @param sql
     * @return the parse sql
     * @throws Exception
     */
    @Throws(MappingNotFoundException::class)
    fun parseSql(sql: String): String {
        var parsedSql = sql
        val list = parsedSql.parseRegexGroup(REGEX)
        for (string in list) {
            val pos = string.lastIndexOf(Constants.Symbol.DOT)
            if (pos > 0) {
                val className = string.substring(0, pos)
                val fieldName = string.substring(pos + 1, string.length)
                val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(className)
                if (mappingBean != null) {
                    val columnName = mappingBean.getColumn(fieldName)
                    if (columnName.isNotBlank()) {
                        parsedSql = parsedSql.replaceFirst(REGEX.toRegex(), Constants.Symbol.ACCENT + columnName + Constants.Symbol.ACCENT)
                    } else {
                        throw MappingNotFoundException("can not find the mapping field: " + className + Constants.Symbol.DOT + fieldName)
                    }
                } else {
                    throw MappingNotFoundException("can not find the mapping bean: $className")
                }
            } else {
                val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(string)
                if (mappingBean != null) {
                    val schema = mappingBean.schema
                    val table = mappingBean.table
                    if (table.isNotBlank()) {
                        parsedSql = if (schema.isNotBlank()) {
                            parsedSql.replaceFirst(REGEX.toRegex(), Constants.Symbol.ACCENT + schema + Constants.Symbol.ACCENT + Constants.Symbol.DOT + Constants.Symbol.ACCENT + table + Constants.Symbol.ACCENT)
                        } else {
                            parsedSql.replaceFirst(REGEX.toRegex(), Constants.Symbol.ACCENT + table + Constants.Symbol.ACCENT)
                        }
                    } else {
                        throw MappingNotFoundException("can not find the mapping table of the class:$string")
                    }
                } else {
                    throw MappingNotFoundException("can not find the mapping class:$string")
                }
            }
        }
        return parsedSql
    }
}
