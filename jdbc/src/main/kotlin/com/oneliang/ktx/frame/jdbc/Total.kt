package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

class Total {
    companion object {
        const val FIELD_TOTAL = "total"
    }

    var total: Int = 0
}

internal fun Total.Companion.toMappingBean(): MappingBean {
    return MappingBean().apply {
        this.schema = Constants.String.BLANK
        this.table = Constants.Database.COLUMN_NAME_TOTAL
        this.type = Total::class.java.name
        this.addMappingColumnBean(MappingColumnBean().apply {
            this.field = FIELD_TOTAL
            this.column = Constants.Database.COLUMN_NAME_TOTAL
        })
    }
}