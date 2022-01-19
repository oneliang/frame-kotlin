package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.parseXml
import com.oneliang.ktx.util.common.toFile

class ModelTemplateBean {
    companion object {
        const val TAG_MODEL = "model"
        const val TAG_PACKAGE_NAME = "packageName"
        const val TAG_CLASS_NAME = "className"
        const val TAG_SCHEMA = "schema"
        const val TAG_TABLE = "table"

        const val TAG_MODEL_IMPORT = "import"
        const val TAG_MODEL_IMPORT_VALUE = "value"
        const val TAG_MODEL_COLUMN = "column"
        const val TAG_MODEL_COLUMN_FIELD = "field"
        const val TAG_MODEL_COLUMN_COLUMN = "column"
        const val TAG_MODEL_COLUMN_ID_FLAG = "idFlag"
        const val TAG_MODEL_COLUMN_TYPE = "type"
        const val TAG_MODEL_COLUMN_NULLABLE = "nullable"
        const val TAG_MODEL_COLUMN_DEFAULT_VALUE = "defaultValue"
    }
    var packageName = Constants.String.BLANK
    var importArray = emptyArray<String>()
    var className = Constants.String.BLANK
    var schema = Constants.String.BLANK
    var table = Constants.String.BLANK
    var columnArray = emptyArray<Column>()

    class Column {
        enum class Type(val label: String, val value: Int) {
            STRING("STRING", 0),
            INT("INT", 1),
            LONG("LONG", 2),
            FLOAT("FLOAT", 3),
            DOUBLE("DOUBLE", 4),
            DATE("DATE", 5)
        }

        var field: String = Constants.String.BLANK
        var column: String = Constants.String.BLANK
        var idFlag: Boolean = false
        var type: Int = Type.STRING.value
        var nullable: Boolean = false
        var defaultValue: String = Constants.String.BLANK
    }
}