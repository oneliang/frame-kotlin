package com.oneliang.ktx.frame.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

open class GenerateService {
    companion object {
        const val NAME = "generateService"
    }

    var modelNameMap: Map<String, Array<String>> = HashMap()
    var serviceTemplate: File? = null
    var serviceImplTemplate: File? = null
    var toDirectory: File? = null
}

fun Project.generateService(configuration: Action<GenerateService>) {
    project.extensions.configure(GenerateService.NAME, configuration)
}