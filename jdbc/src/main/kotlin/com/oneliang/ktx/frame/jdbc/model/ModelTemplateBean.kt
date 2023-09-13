package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toNewArray

class ModelTemplateBean : TableModel() {
    companion object {
        const val TAG_MODEL = "model"
        const val ATTRIBUTE_MODEL_PACKAGE_NAME = "packageName"
        const val ATTRIBUTE_MODEL_CLASS_NAME = "className"
        const val ATTRIBUTE_MODEL_SUPER_CLASS_NAMES = "superClassNames"
        const val ATTRIBUTE_MODEL_SCHEMA = "schema"
        const val ATTRIBUTE_MODEL_TABLE = "table"
        const val ATTRIBUTE_MODEL_COMMENT = "comment"

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

    //    var schema = Constants.String.BLANK//in super class
//    var table = Constants.String.BLANK//in super class
    var fieldArray = emptyArray<Field>()
    var tableIndexArray = emptyArray<TableIndex>()
    var codeInClassArray = emptyArray<String>()

    class Field : ColumnModel() {
        var override: Boolean = false
        var name: String = Constants.String.BLANK

        //        var type: String = SqlUtil.ColumnType.STRING.value//in super class
//        var nullable: Boolean = false//in super class
        var defaultValue: String = Constants.String.BLANK
//        var column: String = Constants.String.BLANK//in super class
//        var idFlag: Boolean = false//in super class
//        var columnDefaultValue: String = Constants.String.BLANK//in super class
//        var length: Int = 0//in super class
//        var precision: Int = 0//in super class
//        var comment: String = Constants.String.BLANK//in super class

        fun toColumnModel(): ColumnModel {
            val columnModel = ColumnModel()
            columnModel.type = this.type
            columnModel.nullable = this.nullable
            columnModel.column = this.column
            columnModel.idFlag = this.idFlag
            columnModel.columnDefaultValue = this.columnDefaultValue
            columnModel.length = this.length
            columnModel.precision = this.precision
            columnModel.comment = this.comment
            return columnModel
        }
    }

    class TableIndex : IndexModel() {
        //        var columns: String = Constants.String.BLANK//in super class
//        var otherCommands: String = Constants.String.BLANK//in super class
        fun toIndexModel(): IndexModel {
            val indexModel = IndexModel()
            indexModel.columns = this.columns
            indexModel.otherCommands = this.otherCommands
            return indexModel
        }
    }

    fun toTableModel(): TableModel {
        val tableModel = TableModel()
        tableModel.schema = this.schema
        tableModel.table = this.table
        tableModel.columnArray = this.fieldArray.toNewArray { it.toColumnModel() }
        tableModel.indexArray = this.tableIndexArray.toNewArray { it.toIndexModel() }
        return tableModel
    }
}