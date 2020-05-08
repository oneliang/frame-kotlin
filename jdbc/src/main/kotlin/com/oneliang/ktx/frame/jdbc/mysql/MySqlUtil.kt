package com.oneliang.ktx.frame.jdbc.mysql

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.bean.Page
import com.oneliang.ktx.frame.jdbc.SqlUtil

object MySqlUtil {
    fun selectPaginationSql(table: String, condition: String = Constants.String.BLANK, sequenceKey: String, startSequence: String, orderBy: String, rowPerPage: Int = Page.DEFAULT_ROWS): String {
        val paginationCondition = "$condition AND $sequenceKey > $startSequence $orderBy ${Constants.Database.MySql.PAGINATION} 0,$rowPerPage"
        return SqlUtil.selectSql(table = table, condition = paginationCondition)
    }
}