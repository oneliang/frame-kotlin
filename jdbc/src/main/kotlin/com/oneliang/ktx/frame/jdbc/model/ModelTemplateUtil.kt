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
            modelTemplateBean.packageName = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_PACKAGE_NAME)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            modelTemplateBean.className = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_CLASS_NAME)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            modelTemplateBean.superClassNames = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_SUPER_CLASS_NAMES)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            modelTemplateBean.schema = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_SCHEMA)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            modelTemplateBean.table = modelAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_TABLE)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
            val importList = mutableListOf<String>()
            val columnList = mutableListOf<ModelTemplateBean.Field>()
            val codeInClassList = mutableListOf<String>()
            val modelChildNodeList = modelNode.childNodes
            for (modelChildNodeIndex in 0 until modelChildNodeList.length) {
                val modelChildNode = modelChildNodeList.item(modelChildNodeIndex)
                val modelChildNodeAttributeMap = modelChildNode.attributes
                when (modelChildNode.nodeName) {
                    ModelTemplateBean.TAG_MODEL_IMPORT -> {
                        importList += modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_IMPORT_VALUE).nodeValue
                    }
                    ModelTemplateBean.TAG_MODEL_FIELD -> {
                        //model field
                        val field = ModelTemplateBean.Field()
                        val overrideNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_OVERRIDE)
                        field.override = overrideNode?.nodeValue?.toBoolean() ?: false
                        field.name = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_NAME)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
                        val typeNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_TYPE)
                        if (typeNode != null) {
                            field.type = when (typeNode.nodeValue) {
                                ModelTemplateBean.Field.Type.INT.label -> {
                                    ModelTemplateBean.Field.Type.INT.value
                                }
                                ModelTemplateBean.Field.Type.LONG.label -> {
                                    ModelTemplateBean.Field.Type.LONG.value
                                }
                                ModelTemplateBean.Field.Type.FLOAT.label -> {
                                    ModelTemplateBean.Field.Type.FLOAT.value
                                }
                                ModelTemplateBean.Field.Type.DOUBLE.label -> {
                                    ModelTemplateBean.Field.Type.DOUBLE.value
                                }
                                ModelTemplateBean.Field.Type.DATE.label -> {
                                    ModelTemplateBean.Field.Type.DATE.value
                                }
                                else -> {
                                    ModelTemplateBean.Field.Type.STRING.value
                                }
                            }
                        }
                        val nullableNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_NULLABLE)
                        field.nullable = nullableNode?.nodeValue?.toBoolean() ?: false
                        field.defaultValue = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_DEFAULT_VALUE)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
                        //db mapping attribute
                        field.column = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_COLUMN)?.nodeValue?.nullToBlank() ?: Constants.String.BLANK
                        val idFlagNode = modelChildNodeAttributeMap.getNamedItem(ModelTemplateBean.ATTRIBUTE_MODEL_FIELD_ID_FLAG)
                        field.idFlag = idFlagNode?.nodeValue?.toBoolean() ?: false

                        columnList += field
                    }
                    ModelTemplateBean.TAG_MODEL_CODE_IN_CLASS -> {
                        codeInClassList += modelChildNode.textContent
                    }
                }
            }
            modelTemplateBean.importArray = importList.toTypedArray()
            modelTemplateBean.fieldArray = columnList.toTypedArray()
            modelTemplateBean.codeInClassArray = codeInClassList.toTypedArray()

            modelTemplateBeanList += modelTemplateBean
        }
        return modelTemplateBeanList
    }
}