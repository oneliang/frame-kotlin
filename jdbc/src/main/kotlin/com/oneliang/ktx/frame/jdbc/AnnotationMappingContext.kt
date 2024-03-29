package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.reflect.KClass

class AnnotationMappingContext : MappingContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationMappingContext::class)
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        if (fixParameters.isBlank()) {
            logger.warning("parameters is blank, maybe use dsl initialize, please confirm it.")
            return
        }
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Table::class)
            for (kClass in classList) {
                logger.debug("Annotation table mapping class:%s", kClass)
                processClass(kClass)
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * process class to context
     * @param kClass
     */
    fun processClass(kClass: KClass<*>) {
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
            val annotationMappingColumnBean = AnnotationMappingColumnBean.build(
                columnAnnotation.field,
                columnAnnotation.column,
                columnAnnotation.idFlag,
                columnAnnotation.condition
            )
            annotationMappingBean.addMappingColumnBean(annotationMappingColumnBean)
        }
        val indexAnnotations = tableAnnotation.indexes
        for (indexAnnotation in indexAnnotations) {
            val annotationMappingIndexBean = AnnotationMappingIndexBean.build(
                indexAnnotation.columns,
                indexAnnotation.otherCommands
            )
            annotationMappingBean.addMappingIndexBean(annotationMappingIndexBean)
        }
        val fields = kClass.java.declaredFields
        if (!fields.isNullOrEmpty()) {
            for (field in fields) {
                if (field.isAnnotationPresent(Table.Column::class.java)) {
                    val columnAnnotation = field.getAnnotation(Table.Column::class.java)
                    val annotationMappingColumnBean = AnnotationMappingColumnBean.build(
                        field.name,
                        columnAnnotation.column,
                        columnAnnotation.idFlag,
                        columnAnnotation.condition
                    )
                    annotationMappingBean.addMappingColumnBean(annotationMappingColumnBean)
                }
            }
        }
        classNameMappingBeanMap[className] = annotationMappingBean
        simpleNameMappingBeanMap[classSimpleName] = annotationMappingBean
    }
}
