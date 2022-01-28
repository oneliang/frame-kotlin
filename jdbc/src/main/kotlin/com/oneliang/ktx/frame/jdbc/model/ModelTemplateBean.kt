package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jdbc.SqlUtil

class ModelTemplateBean {
    companion object {
        const val TAG_MODEL = "model"
        const val ATTRIBUTE_MODEL_PACKAGE_NAME = "packageName"
        const val ATTRIBUTE_MODEL_CLASS_NAME = "className"
        const val ATTRIBUTE_MODEL_SUPER_CLASS_NAMES = "superClassNames"
        const val ATTRIBUTE_MODEL_SCHEMA = "schema"
        const val ATTRIBUTE_MODEL_TABLE = "table"
        //import
        const val TAG_MODEL_IMPORT = "import"
        const val ATTRIBUTE_MODEL_IMPORT_VALUE = "value"
        //field
        const val TAG_MODEL_FIELD = "field"
        const val ATTRIBUTE_MODEL_FIELD_OVERRIDE = "override"
        const val ATTRIBUTE_MODEL_FIELD_NAME = "name"
        const val ATTRIBUTE_MODEL_FIELD_TYPE = "type"
        const val ATTRIBUTE_MODEL_FIELD_NULLABLE = "nullable"
        const val ATTRIBUTE_MODEL_FIELD_DEFAULT_VALUE = "defaultValue"
        const val ATTRIBUTE_MODEL_FIELD_COLUMN = "column"
        const val ATTRIBUTE_MODEL_FIELD_ID_FLAG = "idFlag"
        const val ATTRIBUTE_MODEL_FIELD_COLUMN_DEFAULT_VALUE = "columnDefaultValue"
        const val ATTRIBUTE_MODEL_FIELD_LENGTH = "length"
        const val ATTRIBUTE_MODEL_FIELD_PRECISION = "precision"
        const val ATTRIBUTE_MODEL_FIELD_COMMENT = "comment"
        //tableIndex
        const val TAG_MODEL_TABLE_INDEX = "tableIndex"
        const val ATTRIBUTE_MODEL_TABLE_INDEX_COLUMNS = "columns"
        const val ATTRIBUTE_MODEL_TABLE_INDEX_OTHER_COMMANDS = "otherCommands"
        //codeInClass
        const val TAG_MODEL_CODE_IN_CLASS = "codeInClass"
    }

    var packageName = Constants.String.BLANK
    var importArray = emptyArray<String>()
    var className = Constants.String.BLANK
    var superClassNames = Constants.String.BLANK
    var schema = Constants.String.BLANK
    var table = Constants.String.BLANK
    var fieldArray = emptyArray<Field>()
    var tableIndexArray = emptyArray<TableIndex>()
    var codeInClassArray = emptyArray<String>()

    class Field {
        var override: Boolean = false
        var name: String = Constants.String.BLANK
        var type: String = SqlUtil.ColumnType.STRING.value
        var nullable: Boolean = false
        var defaultValue: String = Constants.String.BLANK
        var column: String = Constants.String.BLANK
        var idFlag: Boolean = false
        var columnDefaultValue: String = Constants.String.BLANK
        var length: Int = 0
        var precision: Int = 0
        var comment: String = Constants.String.BLANK
    }

    class TableIndex {
        var columns: String = Constants.String.BLANK
        var otherCommands: String = Constants.String.BLANK
    }
}