package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants

class ModelTemplateBean {
    companion object {
        const val TAG_MODEL = "model"
        const val ATTRIBUTE_MODEL_PACKAGE_NAME = "packageName"
        const val ATTRIBUTE_MODEL_CLASS_NAME = "className"
        const val ATTRIBUTE_MODEL_SUPER_CLASS_NAMES = "superClassNames"
        const val ATTRIBUTE_MODEL_SCHEMA = "schema"
        const val ATTRIBUTE_MODEL_TABLE = "table"

        const val TAG_MODEL_IMPORT = "import"
        const val ATTRIBUTE_MODEL_IMPORT_VALUE = "value"
        const val TAG_MODEL_FIELD = "field"
        const val ATTRIBUTE_MODEL_FIELD_OVERRIDE = "override"
        const val ATTRIBUTE_MODEL_FIELD_NAME = "name"
        const val ATTRIBUTE_MODEL_FIELD_TYPE = "type"
        const val ATTRIBUTE_MODEL_FIELD_NULLABLE = "nullable"
        const val ATTRIBUTE_MODEL_FIELD_DEFAULT_VALUE = "defaultValue"
        const val ATTRIBUTE_MODEL_FIELD_COLUMN = "column"
        const val ATTRIBUTE_MODEL_FIELD_ID_FLAG = "idFlag"

        const val TAG_MODEL_CODE_IN_CLASS = "codeInClass"
    }

    var packageName = Constants.String.BLANK
    var importArray = emptyArray<String>()
    var className = Constants.String.BLANK
    var superClassNames = Constants.String.BLANK
    var schema = Constants.String.BLANK
    var table = Constants.String.BLANK
    var fieldArray = emptyArray<Field>()
    var codeInClassArray = emptyArray<String>()

    class Field {
        enum class Type(val label: String, val value: Int) {
            STRING("STRING", 0),
            INT("INT", 1),
            LONG("LONG", 2),
            FLOAT("FLOAT", 3),
            DOUBLE("DOUBLE", 4),
            DATE("DATE", 5),
            BIG_DECIMAL("BIG_DECIMAL", 6),
        }

        var override: Boolean = false
        var name: String = Constants.String.BLANK
        var type: Int = Type.STRING.value
        var nullable: Boolean = false
        var defaultValue: String = Constants.String.BLANK
        var column: String = Constants.String.BLANK
        var idFlag: Boolean = false
    }
}