package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager

class AnnotationMappingContext : MappingContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationMappingContext::class)
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Table::class)
            for (kClass in classList) {
                logger.info("Annotation table mapping class:%s", kClass.toString())
                val className = kClass.java.name
                val classSimpleName = kClass.java.simpleName
                val tableAnnotation = kClass.java.getAnnotation(Table::class.java)
                val annotationMappingBean = AnnotationMappingBean()
                annotationMappingBean.isDropIfExist = tableAnnotation.dropIfExist
                annotationMappingBean.schema = tableAnnotation.schema
                annotationMappingBean.table = tableAnnotation.table
                annotationMappingBean.type = className
                annotationMappingBean.condition = tableAnnotation.condition
                val columnAnnotations = tableAnnotation.columns
                for (columnAnnotation in columnAnnotations) {
                    val annotationMappingColumnBean = AnnotationMappingColumnBean()
                    annotationMappingColumnBean.field = columnAnnotation.field
                    annotationMappingColumnBean.column = columnAnnotation.column
                    annotationMappingColumnBean.isId = columnAnnotation.isId
                    annotationMappingColumnBean.condition = columnAnnotation.condition
                    annotationMappingBean.addMappingColumnBean(annotationMappingColumnBean)
                }
                val fields = kClass.java.declaredFields
                if (fields != null) {
                    for (field in fields) {
                        if (field.isAnnotationPresent(Table.Column::class.java)) {
                            val columnAnnotation = field.getAnnotation(Table.Column::class.java)
                            val annotationMappingColumnBean = AnnotationMappingColumnBean()
                            annotationMappingColumnBean.field = field.name
                            annotationMappingColumnBean.column = columnAnnotation.column
                            annotationMappingColumnBean.isId = columnAnnotation.isId
                            annotationMappingColumnBean.condition = columnAnnotation.condition
                            annotationMappingBean.addMappingColumnBean(annotationMappingColumnBean)
                        }
                    }
                }
                annotationMappingBean.createTableSqls = SqlUtil.createTableSqls(annotationMappingBean)
                classNameMappingBeanMap[className] = annotationMappingBean
                simpleNameMappingBeanMap[classSimpleName] = annotationMappingBean
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }
}
