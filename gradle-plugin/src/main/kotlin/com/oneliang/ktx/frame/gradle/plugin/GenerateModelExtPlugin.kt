package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jdbc.model.ModelTemplateBean
import com.oneliang.ktx.frame.jdbc.model.ModelTemplateUtil
import com.oneliang.ktx.util.generate.Template
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateModelExtPlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "generateModelExt"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, GenerateModelExt::class.java)
        project.afterEvaluate {
            try {
                project.extensions.findByName(EXTENSION_NAME)
            } catch (t: Throwable) {
                t.printStackTrace()
                return@afterEvaluate
            }
            val generateModelExt = project.extensions.findByName(EXTENSION_NAME) as GenerateModelExt
            if (generateModelExt.modelXmlArray.isEmpty()) {
                return@afterEvaluate
            }
            val modelXmlArray = generateModelExt.modelXmlArray
            val ignoreModelClassNameSet = generateModelExt.ignoreModelClassNameSet
            val modelExtensionsTemplate =
                generateModelExt.modelExtensionsTemplate ?: throw RuntimeException("parameter(modelExtensionsTemplate) error, please input parameter(modelExtensionsTemplate).")
            val toDirectory = generateModelExt.toDirectory ?: throw RuntimeException("parameter(toDirectory) error, please input parameter(toDirectory).")
            val toDirectoryAbsolutePath = toDirectory.absolutePath
            val apiModelPackageName = generateModelExt.apiModelPackageName
            val modelExtensionsPackageName = generateModelExt.modelExtensionsPackageName
            val modelTemplateBeanList = mutableListOf<ModelTemplateBean>()
            for (modelXml in modelXmlArray) {
                modelTemplateBeanList.addAll(ModelTemplateUtil.buildModelTemplateBeanListFromXml(modelXml))
            }
            modelTemplateBeanList.forEach { modelTemplateBean ->
                if (ignoreModelClassNameSet.contains(modelTemplateBean.className)) {
                    return@forEach//continue
                }
                val modelExtensionsSubDirectory = modelExtensionsPackageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                val modelExtensionsFullFilename =
                    toDirectoryAbsolutePath + Constants.Symbol.SLASH_LEFT + modelExtensionsSubDirectory + Constants.Symbol.SLASH_LEFT + modelTemplateBean.className + "Extensions" + Constants.Symbol.DOT + "kt"
                val option = Template.Option()
                option.showLog = false
                option.removeBlankLine = true
                option.instance = modelTemplateBean
                option.instanceExtendValueMap = mapOf(
                    "apiModelPackageName" to apiModelPackageName,
                    "modelExtensionsPackageName" to modelExtensionsPackageName
                )
                Template.generate(modelExtensionsTemplate, modelExtensionsFullFilename, option)
            }
        }
    }
}