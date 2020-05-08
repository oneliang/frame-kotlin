package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MappingNotFoundException
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.bean.Page
import kotlin.reflect.KClass

class OracleQueryImpl : DefaultQueryImpl() {

    /**
     * Method: select object pagination list,has implement,it is sql binding
     * @param <T>
     * @param kClass
     * @param page
     * @param countColumn
     * @param selectColumns
     * @param table
     * @param condition
     * @param parameters
     * @return List<T>
     * @throws QueryException
    </T></T> */
    @Throws(QueryException::class)
    override fun <T : Any> selectObjectPaginationList(kClass: KClass<T>, page: Page, countColumn: String, selectColumns: Array<String>, table: String, condition: String, parameters: Array<*>): List<T> {
        var tempSelectColumns = selectColumns
        var tempTable = table
        val totalRows = this.totalRows(kClass, countColumn, tempTable, condition, parameters)
        val rowsPerPage = page.rowsPerPage
        page.initialize(totalRows, rowsPerPage)
        val startRow = page.pageFirstRow
        val currentPage = page.page
        //generate table string
        val rowNumAlias = "rn"
        val tableAlias = "t"
        if (tempSelectColumns.isEmpty()) {
            tempSelectColumns = arrayOf(tableAlias + Constants.Symbol.DOT + Constants.Symbol.WILDCARD)
        }
        val newColumns = Array(tempSelectColumns.size + 1) { Constants.String.BLANK }
        System.arraycopy(tempSelectColumns, 0, newColumns, 0, tempSelectColumns.size)
        newColumns[tempSelectColumns.size] = "rownum $rowNumAlias"
        if (tempTable.isBlank()) {
            val mappingBean = ConfigurationContainer.rootConfigurationContext.findMappingBean(kClass) ?: throw MappingNotFoundException("can not find the mapping bean: $kClass")
            tempTable = mappingBean.table
        }
        tempTable = "$tempTable $tableAlias"
        tempTable = SqlUtil.selectSql(newColumns, tempTable, condition)
        tempTable = Constants.Symbol.BRACKET_LEFT + tempTable + Constants.Symbol.BRACKET_RIGHT
        //generate outer conditions
        val sqlConditions = StringBuilder()
        sqlConditions.append("and " + rowNumAlias + ">" + startRow + " and " + rowNumAlias + "<=" + rowsPerPage * currentPage)
        return useConnection {
            this.executeQuery(it, kClass, emptyArray(), tempTable, sqlConditions.toString(), parameters)
        }
    }
}
