package com.oneliang.ktx.frame.test.api

import com.oneliang.ktx.frame.api.HttpApiGenerator

fun main() {
    val fullFilename = "D:/Dandelion/java/githubWorkspace/frame-kotlin/api/src/main/resources/login.txt"
    val packageName = "com.oneliang.ktx.frame.test.api"
    val apiTemplateFullFilename = "D:/Dandelion/java/githubWorkspace/frame-kotlin/api/src/main/resources/http-api.tmpl"
    val apiRequestTemplateFullFilename = "D:/Dandelion/java/githubWorkspace/frame-kotlin/api/src/main/resources/api-request.tmpl"
    val apiResponseTemplateFullFilename = "D:/Dandelion/java/githubWorkspace/frame-kotlin/api/src/main/resources/api-response.tmpl"
    val baseOutputDirectory = "D:/Dandelion/java/githubWorkspace/frame-kotlin/api/src/test/kotlin"
    HttpApiGenerator.generate(
        listOf(fullFilename),
        packageName,
        apiTemplateFullFilename,
        apiRequestTemplateFullFilename,
        apiResponseTemplateFullFilename,
        baseOutputDirectory
    )
}