package com.oneliang.ktx.frame.test

import com.oneliang.ktx.frame.bean.BeanGenerator

fun main() {
    val fullFilename = "D:/Dandelion/java/githubWorkspace/frame-kotlin/bean/src/main/resources/kotlin-bean.txt"
    val beanTemplateFullFilename = "D:/Dandelion/java/githubWorkspace/frame-kotlin/bean/src/main/resources/kotlin-bean.tmpl"
    val baseOutputDirectory = "D:/Dandelion/java/githubWorkspace/frame-kotlin/bean/src/test/kotlin"
    BeanGenerator.generate(
        listOf(fullFilename),
        beanTemplateFullFilename,
        baseOutputDirectory
    )
}