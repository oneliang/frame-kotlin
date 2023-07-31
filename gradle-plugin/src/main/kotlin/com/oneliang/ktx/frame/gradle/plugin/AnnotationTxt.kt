package com.oneliang.ktx.frame.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project

open class AnnotationTxt {
    companion object {
        const val NAME = "annotationTxt"
    }

    var annotationClassNameArray = emptyArray<String>()
    var fileSuffixArray = emptyArray<String>()
    var showProjectDependencies = false
}

fun Project.annotationTxt(configuration: Action<AnnotationTxt>) {
    project.extensions.configure(AnnotationTxt.NAME, configuration)
}