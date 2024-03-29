package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.reflect.KClass

class AnnotationActionContext : ActionContext() {

    companion object {
        private val logger = LoggerManager.getLogger(AnnotationActionContext::class)
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
            val kClassList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(fixParameters, classLoader, classesRealPath, jarClassLoader, Action::class)
            for (kClass in kClassList) {
                logger.debug("Annotation action class:%s", kClass)
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
        val id = kClass.java.name
        val actionObjectBean: ObjectBean
        val objectBean = objectMap[id]
        if (objectBean == null) {
            val actionInstance = kClass.java.newInstance()
            actionObjectBean = ObjectBean(actionInstance, ObjectBean.Type.REFERENCE)
            objectMap[id] = actionObjectBean
        } else {
            logger.warning("Annotation action class has been instantiated, class:%s, id:%s", kClass, id)
            return
        }
        val methods = kClass.java.methods
        for (method in methods) {
            if (!method.isAnnotationPresent(Action.RequestMapping::class.java)) {
                continue
            }
            val returnType = method.returnType
            if (returnType == String::class.java) {
                val annotationActionBean = AnnotationActionBean()
                val requestMappingAnnotation = method.getAnnotation(Action.RequestMapping::class.java)
                annotationActionBean.type = kClass.java.name
                annotationActionBean.level = when (requestMappingAnnotation.level) {
                    Action.RequestMapping.Level.PUBLIC -> ActionBean.Level.PUBLIC.value
                    else -> ActionBean.Level.PRIVATE.value
                }
                val httpRequestMethods = requestMappingAnnotation.httpRequestMethods.ifEmpty { arrayOf(Constants.Http.RequestMethod.GET, Constants.Http.RequestMethod.POST) }
                annotationActionBean.httpRequestMethods = httpRequestMethods.joinToString(separator = Constants.Symbol.COMMA)
                val httpRequestMethodsCode = annotationActionBean.httpRequestMethodsCode
                val idWithMethodAndHttpRequestMethod = id + Constants.Symbol.DOT + method.name + Constants.Symbol.COMMA + httpRequestMethodsCode
                annotationActionBean.id = idWithMethodAndHttpRequestMethod
                val requestPath = requestMappingAnnotation.value
                annotationActionBean.path = requestPath
                annotationActionBean.method = method
                annotationActionBean.actionObjectBean = actionObjectBean
                actionBeanMap[idWithMethodAndHttpRequestMethod] = annotationActionBean
                val actionBeanList = pathActionBeanMap.getOrPut(requestPath) { mutableListOf() }
                actionBeanList.add(annotationActionBean)
                //interceptor
                val interceptors = requestMappingAnnotation.interceptors
                for (interceptor in interceptors) {
                    val interceptorId = interceptor.id
                    val interceptorMode = interceptor.mode
                    if (interceptorId.isBlank()) {
                        logger.warning("Action interceptor id is blank. @%s, class:%s, method:%s", Action.RequestMapping.Interceptor::class.java.simpleName, kClass.java.name, method.name)
                        continue
                    }
                    val actionInterceptorBean = ActionInterceptorBean()
                    actionInterceptorBean.id = interceptorId
                    when (interceptorMode) {
                        Action.RequestMapping.Interceptor.Mode.BEFORE -> actionInterceptorBean.mode = ActionInterceptorBean.Mode.BEFORE
                        Action.RequestMapping.Interceptor.Mode.AFTER -> actionInterceptorBean.mode = ActionInterceptorBean.Mode.AFTER
                    }
                    annotationActionBean.addActionBeanInterceptor(actionInterceptorBean)
                }
                //static
                val statics = requestMappingAnnotation.statics
                for (staticAnnotation in statics) {
                    val actionForwardBean = ActionForwardBean()
                    actionForwardBean.staticParameters = staticAnnotation.parameters
                    actionForwardBean.staticFilePath = staticAnnotation.filePath
                    annotationActionBean.addActionForwardBean(actionForwardBean)
                }
            } else {
                throw InitializeException("@" + Action.RequestMapping::class.java.simpleName + "class:" + kClass.java.name + ", method:" + method.name + " which the return type must be String.class,current is:" + returnType)
            }
        }
    }
}
