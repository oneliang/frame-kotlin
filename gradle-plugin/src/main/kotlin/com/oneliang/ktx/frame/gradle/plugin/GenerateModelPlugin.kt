package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jdbc.DefaultSqlProcessor
import com.oneliang.ktx.frame.jdbc.SqlUtil
import com.oneliang.ktx.frame.jdbc.model.ModelTemplateBean
import com.oneliang.ktx.frame.jdbc.model.ModelTemplateUtil
import com.oneliang.ktx.frame.jdbc.model.TableModelUtil
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.generate.Template
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateModelPlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "generateModel"
    }


    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, GenerateModel::class.java)
        project.afterEvaluate {
            try {
                project.extensions.findByName(EXTENSION_NAME)
            } catch (t: Throwable) {
                t.printStackTrace()
                return@afterEvaluate
            }
            val generateModel = project.extensions.findByName(EXTENSION_NAME) as GenerateModel
            if (generateModel.modelXmlArray.isEmpty()) {
                return@afterEvaluate
            }
            val modelXmlArray = generateModel.modelXmlArray
            val modelTemplate = generateModel.modelTemplate ?: throw RuntimeException("parameter(modelTemplate) error, please input parameter(modelTemplate).")
            val toDirectory = generateModel.toDirectory ?: throw RuntimeException("parameter(toDirectory) error, please input parameter(toDirectory).")
            val toDirectoryAbsolutePath = toDirectory.absolutePath
            val modelTemplateBeanList = mutableListOf<ModelTemplateBean>()
            for (modelXml in modelXmlArray) {
                modelTemplateBeanList.addAll(ModelTemplateUtil.buildModelTemplateBeanListFromXml(modelXml))
            }
            val createTableSqlStringBuilder = StringBuilder()
            val defaultSqlProcessor = DefaultSqlProcessor()
            val createTableSqlFile = generateModel.createTableSqlFile
            modelTemplateBeanList.forEach { modelTemplateBean ->
                val subDirectory = modelTemplateBean.packageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                val toFullFilename = toDirectoryAbsolutePath + Constants.Symbol.SLASH_LEFT + subDirectory + Constants.Symbol.SLASH_LEFT + modelTemplateBean.className + Constants.Symbol.DOT + "kt"
                val option = Template.Option()
                option.showLog = false
                option.removeBlankLine = true
                option.instance = modelTemplateBean
                Template.generate(modelTemplate, toFullFilename, option)
                if (createTableSqlFile != null) {
                    val tableModel = modelTemplateBean.toTableModel()
                    createTableSqlStringBuilder.append(TableModelUtil.dropTableSql(tableModel, defaultSqlProcessor as SqlUtil.SqlProcessor))
                    createTableSqlStringBuilder.append(Constants.String.NEW_LINE)
                    createTableSqlStringBuilder.append(TableModelUtil.createTableSql(tableModel, defaultSqlProcessor as SqlUtil.SqlProcessor))
                    createTableSqlStringBuilder.append(Constants.String.NEW_LINE)
                }
            }
            if (createTableSqlFile != null) {
                FileUtil.writeFile(createTableSqlFile, createTableSqlStringBuilder.toString().toByteArray(Charsets.UTF_8), false)
            }
        }
    }
}