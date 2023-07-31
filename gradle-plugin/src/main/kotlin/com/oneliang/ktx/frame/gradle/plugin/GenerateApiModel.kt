package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

open class GenerateApiModel {
    companion object {
        const val NAME = "generateApiModel"
    }

    var modelXmlArray = emptyArray<File>()
    var ignoreModelClassNameSet = emptySet<String>()
    var apiModelPackageName = Constants.String.BLANK
    var apiModelTemplate: File? = null
    var toDirectory: File? = null
}

fun Project.generateApiModel(configuration: Action<GenerateApiModel>) {
    project.extensions.configure(GenerateApiModel.NAME, configuration)
}