package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jdbc.SqlUtil
import com.oneliang.ktx.frame.jdbc.Table
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.parseXml
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.common.toIntSafely
import java.io.File
import java.math.BigDecimal
import java.util.*

object ModelTemplateUtil {

    fun buildModelTemplateBeanListFromXml(modelXml: String): List<ModelTemplateBean> {
        val modelXmlFile = modelXml.toFile()
        return buildModelTemplateBeanListFromXml(modelXmlFile)
    }

    fun buildModelTemplateBeanListFromXml(modelXmlFile: File): List<ModelTemplateBean> {
        if (!modelXmlFile.exists() || !modelXmlFile.isFile) {
            error("xml does not exists or is not a file, input file [%s]".format(modelXmlFile.absolutePath))
        }
        val document = modelXmlFile.parseXml()
        val root = document.documentElement
        val modelElementList = root.getElementsByTagName(ModelTemplateBean.TAG_MODEL)
        val modelTemplateBeanList = mutableListOf<ModelTemplateBean>()
        for (index in 0 until modelElementList.length) {
            val modelTemplateBean = ModelTemplateBean()
            val modelNode = modelElementList.item(index)
            val modelAttributeMap = modelNode.attributes
            modelTemplateBean.packageName = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_PACKAGE_NAME)?.nodeValue ?: Constants.String.BLANK
            modelTemplateBean.className = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_CLASS_NAME)?.nodeValue ?: Constants.String.BLANK
            modelTemplateBean.superClassNames = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_SUPER_CLASS_NAMES)?.nodeValue ?: Constants.String.BLANK
            modelTemplateBean.schema = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_SCHEMA)?.nodeValue ?: Constants.String.BLANK
            modelTemplateBean.table = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_TABLE)?.nodeValue ?: Constants.String.BLANK
            val importHashSet = hashSetOf<String>().apply {
                this += Table::class.qualifiedName.nullToBlank()
            }
            val columnList = mutableListOf<ModelTemplateBean.Field>()
            val tableIndexList = mutableListOf<ModelTemplateBean.TableIndex>()
            val codeInClassList = mutableListOf<String>()
            val modelChildNodeList = modelNode.childNodes
            for (modelChildNodeIndex in 0 until modelChildNodeList.length) {
                val modelChildNode = modelChildNodeList.item(modelChildNodeIndex)
                val modelChildNodeAttributeMap = modelChildNode.attributes
                when (modelChildNode.nodeName) {
                    ModelTemplateBean.TAG_MODEL_IMPORT -> {
                        importHashSet += modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_IMPORT_VALUE).nodeValue
                    }
                    ModelTemplateBean.TAG_MODEL_FIELD -> {
                        //model field
                        val field = ModelTemplateBean.Field()
                        val overrideNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_OVERRIDE)
                        field.override = overrideNode?.nodeValue?.toBoolean() ?: false
                        field.name = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_NAME)?.nodeValue ?: Constants.String.BLANK
                        val type = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_TYPE)?.nodeValue ?: field.type
                        field.type = type
                        when (type) {
                            SqlUtil.ColumnType.DATE.value -> {
                                importHashSet += Date::class.qualifiedName.nullToBlank()
                            }
                            SqlUtil.ColumnType.BIG_DECIMAL.value -> {
                                importHashSet += BigDecimal::class.qualifiedName.nullToBlank()
                            }
                        }
                        val nullableNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_NULLABLE)
                        field.nullable = nullableNode?.nodeValue?.toBoolean() ?: false
                        field.defaultValue = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_DEFAULT_VALUE)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
                        //db mapping attribute
                        field.column = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_COLUMN)?.nodeValue ?: Constants.String.BLANK
                        val idFlagNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_ID_FLAG)
                        field.idFlag = idFlagNode?.nodeValue?.toBoolean() ?: false
                        field.columnDefaultValue = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_COLUMN_DEFAULT_VALUE)?.nodeValue ?: Constants.String.BLANK
                        field.length = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_LENGTH)?.nodeValue.toIntSafely()
                        field.precision = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_PRECISION)?.nodeValue.toIntSafely()
                        field.comment = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_COMMENT)?.nodeValue ?: Constants.String.BLANK

                        columnList += field
                    }
                    ModelTemplateBean.TAG_MODEL_TABLE_INDEX -> {
                        val tableIndex = ModelTemplateBean.TableIndex()
                        tableIndex.columns = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_TABLE_INDEX_COLUMNS)?.nodeValue ?: Constants.String.BLANK
                        tableIndex.otherCommands = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_TABLE_INDEX_OTHER_COMMANDS)?.nodeValue ?: Constants.String.BLANK
                        tableIndexList += tableIndex
                    }
                    ModelTemplateBean.TAG_MODEL_CODE_IN_CLASS -> {
                        codeInClassList += modelChildNode.textContent
                    }
                }
            }
            modelTemplateBean.importArray = importHashSet.toTypedArray()
            modelTemplateBean.fieldArray = columnList.toTypedArray()
            modelTemplateBean.tableIndexArray = tableIndexList.toTypedArray()
            modelTemplateBean.codeInClassArray = codeInClassList.toTypedArray()

            modelTemplateBeanList += modelTemplateBean
        }
        return modelTemplateBeanList
    }
}

fun main() {
    println(BigDecimal::class.qualifiedName)
}