package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

open class MappingColumnBean {
    companion object {
        const val TAG_COLUMN = "column"
    }
    /**
     * @return the field
     */
    /**
     * @param field the field to set
     */
    var field: String = Constants.String.BLANK
    /**
     * @return the column
     */
    /**
     * @param column the column to set
     */
    var column: String = Constants.String.BLANK
    /**
     * @return the isId
     */
    /**
     * @param isId the isId to set
     */
    var isId = false
}
