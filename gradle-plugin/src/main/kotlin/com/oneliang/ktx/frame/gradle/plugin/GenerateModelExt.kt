package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import org.gradle.api.Action
import org.gradle.api.Project
import java.io.File

open class GenerateModelExt {

    companion object {
        const val NAME = "generateModelExt"
    }

    var modelXmlArray = emptyArray<File>()
    var ignoreModelClassNameArray = emptySet<String>()
    var apiModelPackageName = Constants.String.BLANK
    var modelExtensionsPackageName = Constants.String.BLANK
    var modelExtensionsTemplate: File? = null
    var toDirectory: File? = null
}


fun Project.generateModelExt(configuration: Action<GenerateModelExt>) {
    project.extensions.configure(GenerateModelExt.NAME, configuration)
}