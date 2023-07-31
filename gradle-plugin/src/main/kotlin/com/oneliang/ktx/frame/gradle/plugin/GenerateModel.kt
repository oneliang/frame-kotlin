package com.oneliang.ktx.frame.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

open class GenerateModel {
    companion object {
        const val NAME = "generateModel"
    }

    var modelXmlArray = emptyArray<File>()
    var modelTemplate: File? = null
    var toDirectory: File? = null
    var createTableSqlFile: File? = null
}


fun Project.generateModel(configuration: Action<GenerateModel>) {
    project.extensions.configure(GenerateModel.NAME, configuration)
}