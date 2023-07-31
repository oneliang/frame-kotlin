package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.generate.Template
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateServicePlugin : Plugin<Project> {
    companion object {
        private const val EXTENSION_NAME = "generateService"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, GenerateService::class.java)
        project.afterEvaluate {
            try {
                project.extensions.findByName(EXTENSION_NAME)
            } catch (t: Throwable) {
                t.printStackTrace()
                return@afterEvaluate
            }
            val generateService = project.extensions.findByName(EXTENSION_NAME) as GenerateService
            if (generateService.modelNameMap.isEmpty()) {
                return@afterEvaluate
            }
            val modelNameMap = generateService.modelNameMap
            val serviceTemplate = generateService.serviceTemplate ?: throw RuntimeException("parameter(serviceTemplate) error, please input parameter(serviceTemplate).")
            val serviceImplTemplate = generateService.serviceImplTemplate
            val toDirectory = generateService.toDirectory ?: throw RuntimeException("parameter(toDirectory) error, please input parameter(toDirectory).")
            val toDirectoryAbsolutePath = toDirectory.absolutePath
            modelNameMap.keys.forEach { packageName ->
                val modelNameList = modelNameMap[packageName] ?: emptyArray()
                modelNameList.forEach { modelName ->
                    val serviceDirectory = packageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                    val serviceFullFilename =
                        toDirectoryAbsolutePath + Constants.Symbol.SLASH_LEFT + serviceDirectory + Constants.Symbol.SLASH_LEFT + modelName + "Service" + Constants.Symbol.DOT + "kt"
                    val option = Template.Option()
                    option.showLog = false
                    option.removeBlankLine = true
                    option.json = "{'packageName':'$packageName', 'modelName':'$modelName'}"
                    option.rewrite = false
                    Template.generate(serviceTemplate, serviceFullFilename, option)
                    if (serviceImplTemplate != null) {
                        val serviceImplPackageName = packageName + Constants.Symbol.DOT + "impl"
                        val serviceImplDirectory = serviceImplPackageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
                        option.json = "{'packageName':'$serviceImplPackageName', 'modelName':'$modelName'}"
                        val serviceImplFullFilename =
                            toDirectoryAbsolutePath + Constants.Symbol.SLASH_LEFT + serviceImplDirectory + Constants.Symbol.SLASH_LEFT + modelName + "ServiceImpl" + Constants.Symbol.DOT + "kt"
                        Template.generate(serviceImplTemplate, serviceImplFullFilename, option)
                    }
                }
            }
        }
    }
}