package com.oneliang.ktx.frame.bean

import com.oneliang.ktx.Constants
import com.oneliang.ktx.pojo.KeyValue
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.file.readContentIgnoreLine
import java.io.FileNotFoundException

class BeanDescription {
    companion object {
        const val BEGIN = "begin:"
        const val PACKAGE_NAME = "packageName:"
        const val CLASS_NAME = "className:"
        const val FIELDS = "fields:"
        const val FLAG_PACKAGE_NAME = 1 shl 0
        const val FLAG_CLASS_NAME = 1 shl 1
        const val FLAG_FIELDS = 1 shl 5
        const val FLAG_FIELDS_SUB_CLASS_1 = 1 shl 6
    }

    var packageName = Constants.String.BLANK
    var className = Constants.String.BLANK
    var fields = emptyArray<FieldDescription>()

    class FieldDescription(
        name: String = Constants.String.BLANK,
        type: String = Constants.String.BLANK,
        var description: String = Constants.String.BLANK
    ) : KeyValue(name, type) {
        var subFields = emptyArray<FieldDescription>()
    }
}

private fun BeanDescription.Companion.parseField(line: String): BeanDescription.FieldDescription {
    val field = line.split(Constants.String.SPACE)
    val description = line.substring(field[0].length + Constants.String.SPACE.length + field[1].length).trim()
    return BeanDescription.FieldDescription(field[0], field[1], description)
}

fun BeanDescription.Companion.buildListFromFile(fullFilename: String): List<BeanDescription> {
    val beanDescriptionList = mutableListOf<BeanDescription>()
    var beanDescription: BeanDescription? = null
    val file = fullFilename.toFile()
    if (file.exists() && file.isFile) {
        var currentFlag = 0
        file.readContentIgnoreLine {
            val line = it.trim()
            if (line.isBlank()) {
                return@readContentIgnoreLine true//continue
            }
            when {
                line.startsWith(BEGIN) -> {
                    val newBeanDescription = BeanDescription()
                    beanDescriptionList += newBeanDescription
                    beanDescription = newBeanDescription
                }
                line.startsWith(PACKAGE_NAME) -> {
                    currentFlag = 0//reset
                    currentFlag = currentFlag or FLAG_PACKAGE_NAME
                }
                line.startsWith(CLASS_NAME) -> {
                    currentFlag = 0//reset
                    currentFlag = currentFlag or FLAG_CLASS_NAME
                }
                line.startsWith(FIELDS) -> {
                    currentFlag = 0//reset
                    currentFlag = currentFlag or FLAG_FIELDS
                }
                else -> {
                    val currentBeanDescription = beanDescription ?: return@readContentIgnoreLine true
                    when {
                        currentFlag and FLAG_PACKAGE_NAME == FLAG_PACKAGE_NAME -> {
                            currentBeanDescription.packageName = line
                        }
                        currentFlag and FLAG_CLASS_NAME == FLAG_CLASS_NAME -> {
                            currentBeanDescription.className = line
                        }
                        currentFlag and FLAG_FIELDS == FLAG_FIELDS -> {
                            val colonIndex = line.lastIndexOf(Constants.Symbol.COLON)
                            val fieldList = currentBeanDescription.fields.toMutableList()
                            if (colonIndex > 0) {//has array
                                currentFlag = currentFlag or FLAG_FIELDS_SUB_CLASS_1
                                val fieldKey = line.substring(0, colonIndex)
                                fieldList += BeanDescription.FieldDescription(fieldKey, "CLASS")
                                currentBeanDescription.fields = fieldList.toTypedArray()
                            } else {
                                if (currentFlag and FLAG_FIELDS_SUB_CLASS_1 == FLAG_FIELDS_SUB_CLASS_1) {//use sub class
                                    if (currentBeanDescription.fields.isNotEmpty()) {
                                        val lastField = currentBeanDescription.fields[currentBeanDescription.fields.size - 1]
                                        val subFieldList = lastField.subFields.toMutableList()
                                        subFieldList += parseField(line)
                                        lastField.subFields = subFieldList.toTypedArray()
                                    } else {
                                        error("some invoke sequence error, please check it")
                                    }
                                } else {
                                    fieldList += parseField(line)
                                    currentBeanDescription.fields = fieldList.toTypedArray()
                                }
                            }
                        }
                    }
                }
            }
            true
        }
    } else {
        throw FileNotFoundException("file does not exists or file is a directory, file:%s".format(fullFilename))
    }
    return beanDescriptionList
}
