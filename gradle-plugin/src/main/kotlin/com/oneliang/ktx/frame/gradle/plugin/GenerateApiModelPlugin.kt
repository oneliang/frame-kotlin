package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jdbc.model.ModelTemplateBean
import com.oneliang.ktx.frame.jdbc.model.ModelTemplateUtil
import com.oneliang.ktx.util.generate.Template
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateApiModelPlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "generateApiModel"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, GenerateApiModel::class.java)
        project.afterEvaluate {
            try {
                project.extensions.findByName(EXTENSION_NAME)
            } catch (t: Throwable) {
                t.printStackTrace()
                return@afterEvaluate
            }
            val generateApiModel = project.extensions.findByName(EXTENSION_NAME) as GenerateApiModel
            if (generateApiModel.modelXmlArray.isEmpty()) {
                return@afterEvaluate
            }
            val modelXmlArray = generateApiModel.modelXmlArray
            val ignoreModelClassNameSet = generateApiModel.ignoreModelClassNameSet
            val apiModelTemplate = generateApiModel.apiModelTemplate ?: throw RuntimeException("parameter(apiModelTemplate) error, please input parameter(apiModelTemplate).")
            val toDirectory = generateApiModel.toDirectory ?: throw RuntimeException("parameter(toDirectory) error, please input parameter(toDirectory).")
            val apiModelPackageName = generateApiModel.apiModelPackageName
            val toDirectoryAbsolutePath = toDirectory.absolutePath
            val modelTemplateBeanList = mutableListOf<ModelTemplateBean>()
            for (modelXml in modelXmlArray) {
                modelTemplateBeanList.addAll(ModelTemplateUtil.buildModelTemplateBeanListFromXml(modelXml))
            }
            modelTemplateBeanList.forEach { modelTemplateBean ->
                if (ignoreModelClassNameSet.contains(modelTemplateBean.className)) {
                    return@forEach//continue
                }
                val apiSubDirectory = apiModelPackageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                val apiModelFullFilename = toDirectoryAbsolutePath + Constants.Symbol.SLASH_LEFT + apiSubDirectory + Constants.Symbol.SLASH_LEFT + "Api" + modelTemplateBean.className + Constants.Symbol.DOT + "kt"
                val option = Template.Option()
                option.showLog = false
                option.removeBlankLine = true
                option.instance = modelTemplateBean
                option.instanceExtendValueMap = mapOf("apiModelPackageName" to apiModelPackageName)
                Template.generate(apiModelTemplate, apiModelFullFilename, option)
            }
        }
    }
}