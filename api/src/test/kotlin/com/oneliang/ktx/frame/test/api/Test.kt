package com.oneliang.ktx.frame.test.api

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.api.HttpApiGenerator
import java.io.File

fun main() {
    val projectDirectory = File(Constants.String.BLANK).absolutePath
    val fullFilename = "${projectDirectory}/api/src/test/resources/login.txt"
    val fBoxFullFilename = "${projectDirectory}/api/src/test/resources/fBox.txt"
    val packageName = "com.oneliang.ktx.frame.test.api"
    val apiModelPackageName = "com.oneliang.ktx.frame.test.api.model"
    val apiTemplateFullFilename = "${projectDirectory}/api/src/main/resources/http-api.tmpl"
    val apiRequestTemplateFullFilename = "${projectDirectory}/api/src/main/resources/api-request.tmpl"
    val apiResponseTemplateFullFilename = "${projectDirectory}/api/src/main/resources/api-response.tmpl"
    val baseOutputDirectory = "${projectDirectory}/api/src/test/kotlin"
    HttpApiGenerator.generate(
        listOf(fBoxFullFilename, fullFilename),
        packageName,
        apiModelPackageName,
        apiTemplateFullFilename,
        apiRequestTemplateFullFilename,
        apiResponseTemplateFullFilename,
        baseOutputDirectory
    )
}