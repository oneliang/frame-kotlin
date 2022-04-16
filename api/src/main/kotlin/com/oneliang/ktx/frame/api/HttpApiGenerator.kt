package com.oneliang.ktx.frame.api

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.ifNotBlank
import com.oneliang.ktx.util.common.replaceAllSlashToLeft
import com.oneliang.ktx.util.common.toUpperCaseRange
import com.oneliang.ktx.util.generate.Template
import java.io.File

object HttpApiGenerator {

    private const val CLASS_NAME = "className"
    private const val PACKAGE_NAME = "packageName"

    fun generate(
        httpApiDocList: List<String>,
        packageName: String,
        apiTemplateFullFilename: String,
        apiRequestTemplateFullFilename: String,
        apiResponseTemplateFullFilename: String,
        baseOutputDirectory: String
    ) {
        val option = Template.Option().apply {
            this.removeBlankLine = true
//        this.rewrite = false
        }
        val outputDirectory = File(baseOutputDirectory).absolutePath.replaceAllSlashToLeft() + Constants.Symbol.SLASH_LEFT + packageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
        httpApiDocList.forEach { fullFilename ->
            val (filename, httpApiDescriptionList) = HttpApiDescription.buildListFromFile(fullFilename)
            val className = filename.toUpperCaseRange(0, 1)
            option.instanceExtendValueMap = mapOf(CLASS_NAME to className, PACKAGE_NAME to packageName)
            httpApiDescriptionList.forEach { httpApiDescription ->
                option.instance = httpApiDescription
                apiRequestTemplateFullFilename.ifNotBlank {
                    Template.generate(it, outputDirectory + Constants.Symbol.SLASH_LEFT + "Api${className}${httpApiDescription.key.toUpperCaseRange(0, 1)}Request.kt", option)
                }
                apiResponseTemplateFullFilename.ifNotBlank {
                    Template.generate(it, outputDirectory + Constants.Symbol.SLASH_LEFT + "Api${className}${httpApiDescription.key.toUpperCaseRange(0, 1)}Response.kt", option)
                }
            }
            option.instance = mapOf<String, Any>("list" to httpApiDescriptionList)//replace it
            Template.generate(apiTemplateFullFilename, outputDirectory + Constants.Symbol.SLASH_LEFT + "${className}HttpApi.kt", option)
        }
    }
}