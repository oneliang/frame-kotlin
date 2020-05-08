package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank

open class MappingBean {
    companion object {
        const val TAG_BEAN = "bean"
    }

    var schema: String = Constants.String.BLANK
    var table: String = Constants.String.BLANK
    var type: String = Constants.String.BLANK
    val mappingColumnBeanList = mutableListOf<MappingColumnBean>()
    private val mappingColumnBeanMap = mutableMapOf<String, MappingColumnBean>()

    /**
     * get column, when can not find the column because the field had not mapping
     * @param field
     * @return column
     */
    fun getColumn(field: String): String {
        val mappingColumnBean = mappingColumnBeanMap[field]
        return mappingColumnBean?.column.nullToBlank()
    }

    /**
     * get field
     * @param column
     * @return field
     */
    @Deprecated("not used yet")
    fun getField(column: String): String {
        var field: String? = null
        for (mappingColumnBean in mappingColumnBeanList) {
            val fieldColumn = mappingColumnBean.column
            if (fieldColumn.isNotBlank() && fieldColumn == column) {
                field = mappingColumnBean.field
                break
            }
        }
        return field.nullToBlank()
    }

    /**
     * judge the field is id or not
     * @param field
     * @return is id
     */
    fun isId(field: String): Boolean {
        val mappingColumnBean = mappingColumnBeanMap[field]
        mappingColumnBean ?: error("mapping column bean not found, field:$field")
        return mappingColumnBean.isId
    }

    /**
     * @param mappingColumnBean
     * @return boolean
     */
    fun addMappingColumnBean(mappingColumnBean: MappingColumnBean): Boolean {
        if (mappingColumnBean.field.isBlank()) {
            error("mapping column field can not blank")
        }
        this.mappingColumnBeanMap[mappingColumnBean.field] = mappingColumnBean
        return this.mappingColumnBeanList.add(mappingColumnBean)
    }
}
