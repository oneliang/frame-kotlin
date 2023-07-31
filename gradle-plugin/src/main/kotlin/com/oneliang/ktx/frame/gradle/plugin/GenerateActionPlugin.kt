package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.generate.Template
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateActionPlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "generateAction"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, GenerateAction::class.java)
        project.afterEvaluate {
            try {
                project.extensions.findByName(EXTENSION_NAME)
            } catch (t: Throwable) {
                t.printStackTrace()
                return@afterEvaluate
            }
            val generateAction = project.extensions.findByName(EXTENSION_NAME) as GenerateAction
            if (generateAction.modelNameMap.isEmpty()) {
                return@afterEvaluate
            }
            val modelNameMap = generateAction.modelNameMap
            val actionTemplate = generateAction.actionTemplate
                ?: throw RuntimeException("parameter(actionTemplate) error, please input parameter(actionTemplate).")
            val toDirectory = generateAction.toDirectory
                ?: throw RuntimeException("parameter(toDirectory) error, please input parameter(toDirectory).")
            val toDirectoryAbsolutePath = toDirectory.absolutePath
            modelNameMap.keys.forEach { packageName ->
                val modelNameList = modelNameMap[packageName] ?: emptyArray()
                modelNameList.forEach { modelName ->
                    val actionDirectory = packageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                    val actionFullFilename = toDirectoryAbsolutePath + Constants.Symbol.SLASH_LEFT + actionDirectory + Constants.Symbol.SLASH_LEFT + modelName + "Action" + Constants.Symbol.DOT + "kt"
                    val option = Template.Option()
                    option.showLog = false
                    option.removeBlankLine = true
                    option.json = "{'packageName':'$packageName', 'modelName':'$modelName'}"
                    option.rewrite = false
                    Template.generate(actionTemplate, actionFullFilename, option)
                }
            }
        }
    }
}