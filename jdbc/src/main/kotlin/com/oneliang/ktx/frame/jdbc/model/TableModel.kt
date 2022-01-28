package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jdbc.SqlUtil

open class TableModel {
    var schema = Constants.String.BLANK
    var table = Constants.String.BLANK
    var columnArray = emptyArray<ColumnModel>()
    var indexArray = emptyArray<IndexModel>()

    open class ColumnModel {
        var type: String = SqlUtil.ColumnType.STRING.value
        var nullable: Boolean = false
        var column: String = Constants.String.BLANK
        var idFlag: Boolean = false
        var columnDefaultValue: String = Constants.String.BLANK
        var length: Int = 0
        var precision: Int = 0
        var comment: String = Constants.String.BLANK
    }

    open class IndexModel {
        var columns: String = Constants.String.BLANK
        var otherCommands: String = Constants.String.BLANK
    }
}