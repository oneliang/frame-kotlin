package com.oneliang.ktx.frame.jdbc.mysql

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.frame.jdbc.SqlUtil

object MySqlUtil {

    enum class Compare(val value: String) {
        GREATER_THAN(Constants.Symbol.GREATER_THAN),
        LESS_THAN(Constants.Symbol.LESS_THAN)
    }

    fun selectPaginationSql(selectColumns: Array<String> = emptyArray(), table: String, condition: String = Constants.String.BLANK, sequenceKey: String, startSequence: String, compare: Compare = Compare.GREATER_THAN, orderBy: String, rowPerPage: Int = Page.DEFAULT_ROWS): String {
        val paginationCondition = "$condition AND $sequenceKey ${compare.value} $startSequence $orderBy ${Constants.Database.MySql.PAGINATION} 0,$rowPerPage"
        return SqlUtil.selectSql(selectColumns, table, paginationCondition)
    }
}