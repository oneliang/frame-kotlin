package com.oneliang.ktx.frame.jdbc.model

import java.io.File

fun ModelTemplateBean.Companion.buildListFromXml(modelXml: String) = ModelTemplateUtil.buildModelTemplateBeanListFromXml(modelXml)

fun ModelTemplateBean.Companion.buildListFromXml(modelXmlFile: File) = ModelTemplateUtil.buildModelTemplateBeanListFromXml(modelXmlFile)