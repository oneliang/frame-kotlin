package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

class AnnotationMappingColumnBean : MappingColumnBean() {

    companion object
    /**
     * @return the condition
     */
    /**
     * @param condition the condition to set
     */
    var condition: String = Constants.String.BLANK

}

fun AnnotationMappingColumnBean.Companion.build(field: String, column: String, idFlag: Boolean = false, condition: String = Constants.String.BLANK): AnnotationMappingColumnBean {
    val annotationMappingColumnBean = AnnotationMappingColumnBean()
    annotationMappingColumnBean.field = field
    annotationMappingColumnBean.column = column
    annotationMappingColumnBean.idFlag = idFlag
    annotationMappingColumnBean.condition = condition
    return annotationMappingColumnBean
}