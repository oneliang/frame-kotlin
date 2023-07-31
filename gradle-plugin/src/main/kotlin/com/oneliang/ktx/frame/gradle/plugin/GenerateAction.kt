package com.oneliang.ktx.frame.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

open class GenerateAction {
    companion object {
        const val NAME = "generateAction"
    }

    var modelNameMap: Map<String, Array<String>> = HashMap()
    var actionTemplate: File? = null
    var toDirectory: File? = null
}

fun Project.generateAction(configuration: Action<GenerateAction>) {
    project.extensions.configure(GenerateAction.NAME, configuration)
}