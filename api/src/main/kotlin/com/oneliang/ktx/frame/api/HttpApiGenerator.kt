package com.oneliang.ktx.frame.api

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.ifNotBlank
import com.oneliang.ktx.util.common.replaceAllSlashToLeft
import com.oneliang.ktx.util.common.toUpperCaseRange
import com.oneliang.ktx.util.generate.BeanDescription
import com.oneliang.ktx.util.generate.Template
import java.io.File

object HttpApiGenerator {

    private const val PACKAGE_NAME = "packageName"
    private const val IMPORTS = "imports"
    private const val BASE_CLASS_NAME = "baseClassName"
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
            val (filename, importsCollection, httpApiDescriptionList) = HttpApiDescription.buildListFromFile(fullFilename)
            val baseClassName = filename.toUpperCaseRange(0, 1)
            httpApiDescriptionList.forEach { httpApiDescription ->
                if (httpApiDescription.requestParameters.isNotEmpty()) {
                    apiRequestTemplateFullFilename.ifNotBlank {
                        val apiRequestBeanClassName = "Api${baseClassName}${httpApiDescription.key.toUpperCaseRange(0, 1)}Request"
                        val apiRequestBeanDescription = BeanDescription()
                        apiRequestBeanDescription.className = apiRequestBeanClassName
                        apiRequestBeanDescription.fields = httpApiDescription.requestParameters
                        apiRequestBeanDescription.packageName = apiModelPackageName
                        option.instance = apiRequestBeanDescription
                        Template.generate(it, apiModelOutputDirectory + Constants.Symbol.SLASH_LEFT + "${apiRequestBeanClassName}.kt", option)
                    }
                }
                if (httpApiDescription.responseDatas.isNotEmpty()) {
                    apiResponseTemplateFullFilename.ifNotBlank {
                        val apiResponseBeanClassName = "Api${baseClassName}${httpApiDescription.key.toUpperCaseRange(0, 1)}Response"
                        val apiResponseBeanDescription = BeanDescription()
                        apiResponseBeanDescription.className = apiResponseBeanClassName
                        apiResponseBeanDescription.fields = httpApiDescription.requestParameters
                        apiResponseBeanDescription.packageName = apiModelPackageName
                        option.instance = apiResponseBeanDescription
                        Template.generate(it, apiModelOutputDirectory + Constants.Symbol.SLASH_LEFT + "${apiResponseBeanClassName}.kt", option)
                    }
                }
            }
            option.instance = mapOf<String, Any>("list" to httpApiDescriptionList)//replace it
            option.instanceExtendValueMap = mapOf(
                PACKAGE_NAME to packageName,
                IMPORTS to importsCollection,
                BASE_CLASS_NAME to baseClassName,
                API_MODEL_PACKAGE_NAME to apiModelPackageName
            )
            apiTemplateFullFilename.ifNotBlank {
                Template.generate(it, apiOutputDirectory + Constants.Symbol.SLASH_LEFT + "${baseClassName}HttpApi.kt", option)
            }
            //reset after generate
            option.instance = null
            option.instanceExtendValueMap = emptyMap()
        }
    }
}