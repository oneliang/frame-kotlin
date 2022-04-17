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
    private const val API_MODEL_PACKAGE_NAME = "apiModelPackageName"

    fun generate(
        httpApiDocList: List<String>,
        packageName: String,
        apiModelPackageName: String = packageName,
        apiTemplateFullFilename: String,
        apiRequestTemplateFullFilename: String,
        apiResponseTemplateFullFilename: String,
        baseOutputDirectory: String
    ) {
        val option = Template.Option().apply {
            this.removeBlankLine = true
//        this.rewrite = false
        }
        val apiOutputDirectory = File(baseOutputDirectory).absolutePath.replaceAllSlashToLeft() + Constants.Symbol.SLASH_LEFT + packageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
        val apiModelOutputDirectory = File(baseOutputDirectory).absolutePath.replaceAllSlashToLeft() + Constants.Symbol.SLASH_LEFT + apiModelPackageName.replace(Constants.Symbol.DOT, Constants.Symbol.SLASH_LEFT)
        httpApiDocList.forEach { fullFilename ->
            val (filename, httpApiDescriptionList) = HttpApiDescription.buildListFromFile(fullFilename)
            val className = filename.toUpperCaseRange(0, 1)
            option.instanceExtendValueMap = mapOf(CLASS_NAME to className, PACKAGE_NAME to packageName, API_MODEL_PACKAGE_NAME to apiModelPackageName)
            httpApiDescriptionList.forEach { httpApiDescription ->
                option.instance = httpApiDescription
                apiRequestTemplateFullFilename.ifNotBlank {
                    Template.generate(it, apiModelOutputDirectory + Constants.Symbol.SLASH_LEFT + "Api${className}${httpApiDescription.key.toUpperCaseRange(0, 1)}Request.kt", option)
                }
                apiResponseTemplateFullFilename.ifNotBlank {
                    Template.generate(it, apiModelOutputDirectory + Constants.Symbol.SLASH_LEFT + "Api${className}${httpApiDescription.key.toUpperCaseRange(0, 1)}Response.kt", option)
                }
            }
            option.instance = mapOf<String, Any>("list" to httpApiDescriptionList)//replace it
            apiTemplateFullFilename.ifNotBlank {
                Template.generate(it, apiOutputDirectory + Constants.Symbol.SLASH_LEFT + "${className}HttpApi.kt", option)
            }
        }
    }
}