package com.oneliang.ktx.frame.jdbc.mysql

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.frame.jdbc.Query
import com.oneliang.ktx.frame.jdbc.SqlUtil

object MySqlUtil {

    fun selectPaginationSql(selectColumns: Array<String> = emptyArray(), table: String, condition: String = Constants.String.BLANK, sequenceKey: String, startSequence: String, comparator: Query.Comparator = Query.Comparator.GREATER_THAN, orderBy: String, rowPerPage: Int = Page.DEFAULT_ROWS, useDistinct: Boolean = true): String {
        val paginationCondition = "$condition AND $sequenceKey ${comparator.value} $startSequence $orderBy ${Constants.Database.MySql.PAGINATION} 0,$rowPerPage"
        return SqlUtil.selectSql(selectColumns, table, paginationCondition, useDistinct)
    }
}