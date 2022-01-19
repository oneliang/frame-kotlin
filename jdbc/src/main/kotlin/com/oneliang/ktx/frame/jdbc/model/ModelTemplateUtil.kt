package com.oneliang.ktx.frame.jdbc.model

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.parseXml
import com.oneliang.ktx.util.common.toFile
import java.io.File

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
            modelTemplateBean.packageName = modelAttributeMap.getNamedItem(ModelTemplateBean.TAG_PACKAGE_NAME)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            modelTemplateBean.className = modelAttributeMap.getNamedItem(ModelTemplateBean.TAG_CLASS_NAME)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            modelTemplateBean.schema = modelAttributeMap.getNamedItem(ModelTemplateBean.TAG_SCHEMA)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            modelTemplateBean.table = modelAttributeMap.getNamedItem(ModelTemplateBean.TAG_TABLE)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            val importList = mutableListOf<String>()
            val columnList = mutableListOf<ModelTemplateBean.Column>()
            val modelChildNodeList = modelNode.childNodes
            for (modelChildNodeIndex in 0 until modelChildNodeList.length) {
                val modelChildNode = modelChildNodeList.item(modelChildNodeIndex)
                val modelChildNodeAttributeMap = modelChildNode.attributes
                when (modelChildNode.nodeName) {
                    ModelTemplateBean.TAG_MODEL_IMPORT -> {
                        importList += modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.TAG_MODEL_IMPORT_VALUE).nodeValue
                    }
                    ModelTemplateBean.TAG_MODEL_COLUMN -> {
                        val column = ModelTemplateBean.Column()
                        column.field = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.TAG_MODEL_COLUMN_FIELD)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
                        column.column = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.TAG_MODEL_COLUMN_COLUMN)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
                        val idFlagNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.TAG_MODEL_COLUMN_ID_FLAG)
                        column.idFlag = idFlagNode?.nodeValue?.toBoolean() ?: false
                        val typeNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.TAG_MODEL_COLUMN_TYPE)
                        if (typeNode != null) {
                            column.type = when (typeNode.nodeValue) {
                                ModelTemplateBean.Column.Type.INT.label -> {
                                    ModelTemplateBean.Column.Type.INT.value
                                }
                                ModelTemplateBean.Column.Type.LONG.label -> {
                                    ModelTemplateBean.Column.Type.LONG.value
                                }
                                ModelTemplateBean.Column.Type.FLOAT.label -> {
                                    ModelTemplateBean.Column.Type.FLOAT.value
                                }
                                ModelTemplateBean.Column.Type.DOUBLE.label -> {
                                    ModelTemplateBean.Column.Type.DOUBLE.value
                                }
                                ModelTemplateBean.Column.Type.DATE.label -> {
                                    ModelTemplateBean.Column.Type.DATE.value
                                }
                                else -> {
                                    ModelTemplateBean.Column.Type.STRING.value
                                }
                            }
                        }
                        val nullableNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.TAG_MODEL_COLUMN_NULLABLE)
                        column.nullable = nullableNode?.nodeValue?.toBoolean() ?: false
                        column.defaultValue = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.TAG_MODEL_COLUMN_DEFAULT_VALUE)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
                        columnList += column
                    }
                }
            }
            modelTemplateBean.importArray = importList.toTypedArray()
            modelTemplateBean.columnArray = columnList.toTypedArray()

            modelTemplateBeanList += modelTemplateBean
        }
        return modelTemplateBeanList
    }
}