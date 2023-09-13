package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

class AnnotationMappingIndexBean : MappingIndexBean() {
    companion object
}

fun AnnotationMappingIndexBean.Companion.build(columns: String, otherCommands: String = Constants.String.BLANK): AnnotationMappingIndexBean {
    val annotationMappingIndexBean = AnnotationMappingIndexBean()
    annotationMappingIndexBean.columns = columns
    annotationMappingIndexBean.otherCommands = otherCommands
    return annotationMappingIndexBean
}
