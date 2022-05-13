package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.servlet.action.*
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * com.lwx.frame.servlet.Listener.java
 * @author Dandelion
 * This is only one global servletListener in commonFrame
 * @since 2008-07-31
 */
class ActionListener : HttpServlet() {
    companion object {
        private val logger = LoggerManager.getLogger(ActionListener::class)

        private const val INIT_PARAMETER_CLASS_PROCESSOR = "classProcessor"
        private const val INIT_PARAMETER_LIFECYCLE = "lifecycle"
    }

    private var classProcessor = KotlinClassUtil.DEFAULT_KOTLIN_CLASS_PROCESSOR
    private var lifecycle: Lifecycle? = null

    /**
     * Initialization of the servlet. <br></br>
     * @throws ServletException if an error occurs
     */
    @Throws(ServletException::class)
    override fun init() {
        logger.info("System is starting up, listener is initial.")
        val classProcessorClassName = this.getInitParameter(INIT_PARAMETER_CLASS_PROCESSOR)
        if (!classProcessorClassName.isNullOrBlank()) {
            val fixClassProcessorClassName = classProcessorClassName.replaceAllSpace().replaceAllLines()
            try {
                val clazz = Thread.currentThread().contextClassLoader.loadClass(fixClassProcessorClassName)
                if (ObjectUtil.isInterfaceImplement(clazz, KotlinClassUtil.KotlinClassProcessor::class.java)) {
                    this.classProcessor = clazz.newInstance() as KotlinClassUtil.KotlinClassProcessor
                }
            } catch (e: Throwable) {
                logger.error(Constants.String.EXCEPTION, e)
            }
            logger.info("Class processor is initial, class name:%s", fixClassProcessorClassName)
        }
        val lifecycleClassName = this.getInitParameter(INIT_PARAMETER_LIFECYCLE)
        if (!lifecycleClassName.isNullOrBlank()) {
            val fixLifecycleClassName = lifecycleClassName.replaceAllSpace().replaceAllLines()
            try {
                val clazz = Thread.currentThread().contextClassLoader.loadClass(fixLifecycleClassName)
                if (ObjectUtil.isInterfaceImplement(clazz, Lifecycle::class.java)) {
                    this.lifecycle = clazz.newInstance() as Lifecycle
                }
            } catch (e: Throwable) {
                logger.error(Constants.String.EXCEPTION, e)
            }
            logger.info("Lifecycle is initial, class name:%s", fixLifecycleClassName)
        }
    }

    /**
     * Returns information about the servlet, such as
     * author, version, and copyright.
     *
     * @return String information about this servlet
     */
    override fun getServletInfo(): String {
        return this.javaClass.toString()
    }

    @Throws(ServletException::class, IOException::class)
    override fun service(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        //servlet bean
        var servletBean = ActionUtil.servletBean
        if (servletBean == null) {
            servletBean = ActionUtil.ServletBean()
            ActionUtil.servletBean = servletBean
        }
        servletBean.servletContext = this.servletContext
        servletBean.servletRequest = httpServletRequest
        servletBean.servletResponse = httpServletResponse
        //execute default service method,distribute doGet or doPost or other http method
        super.service(httpServletRequest, httpServletResponse)
        //servlet bean request and response set null
        servletBean.servletRequest = null
        servletBean.servletResponse = null
    }

    override fun getLastModified(httpServletRequest: HttpServletRequest): Long {
        //uri
        //		String uri=request.getRequestURI()
        //		int front=request.getContextPath().length()
        //		uri=uri.substring(front,uri.length())
        //		return 1368624759725l
        return super.getLastModified(httpServletRequest)
    }

    /**
     * Destruction of the servlet. <br></br>
     */
    override fun destroy() {
        super.destroy() // Just puts "destroy" string in log
        // Put your code here
        logger.info("System is shutting down,listener is deleting,please wait")
    }

    /**
     * The doHead method of the servlet. <br></br>
     * This method is called when a HTTP head request is received.
     * @param httpServletRequest the request send by the client to the server
     * @param httpServletResponse the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doHead(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        super.doHead(httpServletRequest, httpServletResponse)
        logRequestForOtherCase(httpServletRequest, httpServletResponse, ActionInterface.HttpRequestMethod.HEAD)
    }

    /**
     * The doTrace method of the servlet. <br></br>
     * This method is called when a HTTP trace request is received.
     * @param httpServletRequest the request send by the client to the server
     * @param httpServletResponse the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doTrace(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        super.doTrace(httpServletRequest, httpServletResponse)
        logRequestForOtherCase(httpServletRequest, httpServletResponse, ActionInterface.HttpRequestMethod.TRACE)
    }

    /**
     * The doOptions method of the servlet. <br></br>
     * This method is called when a HTTP options request is received.
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doOptions(request: HttpServletRequest, response: HttpServletResponse) {
        super.doOptions(request, response)
        logRequestForOtherCase(request, response, ActionInterface.HttpRequestMethod.OPTIONS)
    }

    @Throws(ServletException::class, IOException::class)
    private fun logRequestForOtherCase(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        val uri = httpServletRequest.requestURI
        logger.info("It is requesting uri:%s, http method:%s", uri, httpRequestMethod.name)
    }

    /**
     * The doDelete method of the servlet. <br></br>
     * This method is called when a HTTP delete request is received.
     * @param httpServletRequest the request send by the client to the server
     * @param httpServletResponse the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doDelete(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        dispatch(httpServletRequest, httpServletResponse, ActionInterface.HttpRequestMethod.DELETE)
    }

    /**
     * The doGet method of the servlet. <br></br>
     * This method is called when a form has its tag value method equals to get.
     * @param httpServletRequest the request send by the client to the server
     * @param httpServletResponse the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        dispatch(httpServletRequest, httpServletResponse, ActionInterface.HttpRequestMethod.GET)
    }

    /**
     * The doPost method of the servlet. <br></br>
     * This method is called when a form has its tag value method equals to post.
     * @param httpServletRequest the request send by the client to the server
     * @param httpServletResponse the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doPost(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        dispatch(httpServletRequest, httpServletResponse, ActionInterface.HttpRequestMethod.POST)
    }


    /**
     * The doPut method of the servlet. <br></br>
     * This method is called when a HTTP put request is received.
     * @param httpServletRequest the request send by the client to the server
     * @param httpServletResponse the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doPut(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        dispatch(httpServletRequest, httpServletResponse, ActionInterface.HttpRequestMethod.PUT)
    }

    /**
     * dispatch http request
     * @param httpServletRequest
     * @param httpServletResponse
     * @param httpRequestMethod
     * @throws ServletException
     * @throws IOException
     */
    @Throws(ServletException::class, IOException::class)
    private fun dispatch(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        //uri
        var uri = httpServletRequest.requestURI
        logger.info("It is requesting uri:%s, http method:%s", uri, httpRequestMethod.name)

        val front = httpServletRequest.contextPath.length
        uri = uri.substring(front, uri.length)
        logger.info("The request name is:%s", uri)

        httpServletRequest.setAttribute(ConstantsAction.RequestKey.KEY_STRING_CURRENT_REQUEST_URI, uri)

        try {
            this.lifecycle?.onRequest(uri, httpServletRequest, httpServletResponse, httpRequestMethod)
            this.dispatchToDoAction(uri, httpServletRequest, httpServletResponse, httpRequestMethod)
        } finally {
            this.lifecycle?.onResponse(uri, httpServletRequest, httpServletResponse, httpRequestMethod)
        }
    }

    /**
     * dispatch http request
     * @param httpServletRequest
     * @param httpServletResponse
     * @param httpRequestMethod
     * @throws ServletException
     * @throws IOException
     */
    @Throws(ServletException::class, IOException::class)
    private fun dispatchToDoAction(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        val beforeGlobalInterceptorResult = this.doBeforeGlobalInterceptor(uri, httpServletRequest, httpServletResponse)
        if (!beforeGlobalInterceptorResult) {
            return
        }
        val actionResult = doAction(uri, httpServletRequest, httpServletResponse, httpRequestMethod)
        if (!actionResult) {
            return
        }
    }

    /**
     * do before global interceptor
     * @param uri
     * @param httpServletRequest
     * @param httpServletResponse
     * @return boolean
     */
    private fun doBeforeGlobalInterceptor(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Boolean {
        //global interceptor doIntercept
        val beforeGlobalInterceptorBeanIterable = ConfigurationContainer.rootConfigurationContext.beforeGlobalInterceptorBeanIterable
        val beforeGlobalInterceptorResult = doGlobalInterceptorBeanIterable(beforeGlobalInterceptorBeanIterable, httpServletRequest, httpServletResponse)

        //through the interceptor
        if (beforeGlobalInterceptorResult.type == InterceptorInterface.Result.Type.STOP) {
            logger.info("Stopping through the before global interceptors! Maybe special use, like request forward. The request name:%s", uri)
            return false
        } else if (beforeGlobalInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("Can not through the before global interceptors! The request name:%s", uri)
            httpServletResponse.status = Constants.Http.StatusCode.FORBIDDEN
            httpServletResponse.outputStream.write(beforeGlobalInterceptorResult.message)
            return false
        }
        logger.info("Through the before global interceptors! The request name:%s", uri)
        return true
    }

    /**
     * do before action interceptor
     * @param uri
     * @param actionBean
     * @param httpServletRequest
     * @param httpServletResponse
     * @return boolean
     */
    private fun doBeforeActionInterceptor(uri: String, actionBean: ActionBean, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Boolean {
        //action interceptor doIntercept
        val beforeActionBeanInterceptorList = actionBean.beforeActionInterceptorBeanList
        val beforeActionInterceptorResult = doActionInterceptorBeanList(beforeActionBeanInterceptorList, httpServletRequest, httpServletResponse)
        if (beforeActionInterceptorResult.type == InterceptorInterface.Result.Type.STOP) {
            logger.info("Stopping through the before action interceptors! Maybe special use, like request forward. The request name:%s", uri)
            return false
        } else if (beforeActionInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("Can not through the before action interceptors! The request name:%s", uri)
            httpServletResponse.status = Constants.Http.StatusCode.FORBIDDEN
            httpServletResponse.outputStream.write(beforeActionInterceptorResult.message)
            return false
        }
        logger.info("Through the before action interceptors! The request name:%s", uri)
        return true
    }

    /**
     * do action
     * @param uri
     * @param httpServletRequest
     * @param httpServletResponse
     * @return boolean
     * @throws IOException
     * @throws ServletException
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class, ServletException::class, IOException::class)
    private fun doAction(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        val actionBeanList = ConfigurationContainer.rootConfigurationContext.findActionBeanList(uri)
        if (actionBeanList.isNullOrEmpty()) {
            logger.info("The request name:%s. It does not exist, please config the name and entity class", uri)
            httpServletResponse.status = Constants.Http.StatusCode.NOT_FOUND
            return false
        }
        var actionBean: ActionBean? = null
        for (eachActionBean in actionBeanList) {
            if (eachActionBean.isContainHttpRequestMethod(httpRequestMethod)) {
                actionBean = eachActionBean
                break
            }
        }
        if (actionBean == null) {
            logger.info("The request name:%s. Method not allowed, http request method::%s", uri, httpRequestMethod)
            httpServletResponse.status = Constants.Http.StatusCode.METHOD_NOT_ALLOWED
            return false
        }
        val beforeActionInterceptorResult = this.doBeforeActionInterceptor(uri, actionBean, httpServletRequest, httpServletResponse)
        if (!beforeActionInterceptorResult) {
            return false
        }
        val actionInstance = actionBean.actionObjectBean?.instance
        return try {
            if (actionInstance is ActionInterface) {
                doAction(uri, actionBean, httpServletRequest, httpServletResponse, httpRequestMethod)
            } else {
                doAnnotationAction(uri, actionBean, httpServletRequest, httpServletResponse, httpRequestMethod)
            }
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
            logger.info("The request name:%s. Action or page does not exist", uri)
            val exceptionPath = ConfigurationContainer.rootConfigurationContext.globalExceptionForwardPath.nullToBlank()
            if (exceptionPath.isNotBlank()) {
                logger.info("Forward to exception path:%s", exceptionPath)
                httpServletRequest.setAttribute(Constants.String.EXCEPTION, e)
                val requestDispatcher = httpServletRequest.getRequestDispatcher(exceptionPath)
                requestDispatcher.forward(httpServletRequest, httpServletResponse)
            } else {
                logger.info("System can not find the exception path.Please config the global exception forward path.")
                httpServletResponse.status = Constants.Http.StatusCode.INTERNAL_SERVER_ERROR
            }
            false
        }
    }

    /**
     * do action
     * @param uri
     * @param actionBean
     * @param httpServletRequest
     * @param httpServletResponse
     * @param httpRequestMethod
     * @return boolean
     */
    @Throws(ActionExecuteException::class, ServletException::class, IOException::class)
    private fun doAction(uri: String, actionBean: ActionBean, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        val actionInstance = actionBean.actionObjectBean?.instance
        if (actionInstance !is ActionInterface) {
            logger.error("It is not ActionInterface, actionBean:%s, it is impossible", actionBean)
            return false
        }
        logger.info("Action implements (%s) is executing", actionInstance)
        //judge is it contain static file page
        val parameterMap = httpServletRequest.parameterMap as Map<String, Array<String>>
        val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
        val (normalExecute, needToStaticExecute) = this.getExecuteType(actionForwardBean)
        val forward = if (normalExecute || needToStaticExecute) {
            if (normalExecute) {
                logger.info("Normal executing")
            } else if (needToStaticExecute) {
                logger.info("Need to static execute, first time executing original action")
            }
            actionInstance.execute(httpServletRequest, httpServletResponse)
        } else {
            logger.info("Static execute, not the first time execute")
            actionForwardBean!!.name
        }
        val afterActionInterceptorResult = this.doAfterActionInterceptor(uri, actionBean, httpServletRequest, httpServletResponse)
        if (!afterActionInterceptorResult) {
            return false
        }

        val afterGlobalInterceptorResult = this.doAfterGlobalInterceptor(uri, httpServletRequest, httpServletResponse)
        if (!afterGlobalInterceptorResult) {
            return false
        }

        if (forward.isNotBlank()) {
            var path = actionBean.findForwardPath(forward)
            if (path.isNotBlank()) {
                logger.info("The forward name in configFile is actionPath:%s, forward:%s, path:%s", actionBean.path, forward, path)
            } else {
                path = ConfigurationContainer.rootConfigurationContext.findGlobalForwardPath(forward)
                logger.info("The forward name in global forward configFile is forward:%s, path:%s", forward, path)
            }
            this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, httpServletRequest, httpServletResponse, false)
        } else {
            logger.info("The forward name:%s does not exist, may be ajax use if not please config the name and entity page or class", forward)
        }
        return true
    }

    /**
     * @param annotationActionBean
     * @param httpServletRequest
     * @param httpServletResponse
     * @return Object[]
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
    private fun annotationActionMethodParameterValues(annotationActionBean: AnnotationActionBean, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Array<Any?> {
        val annotationActionBeanMethod = annotationActionBean.method!!
        val classes = annotationActionBeanMethod.parameterTypes
        val parameterValues = arrayOfNulls<Any>(classes.size)
        val annotations = annotationActionBeanMethod.parameterAnnotations
        for (i in annotations.indices) {
            if (annotations[i].isNotEmpty() && annotations[i][0] is Action.RequestMapping.RequestParameter) {
                val requestParameterAnnotation = annotations[i][0] as Action.RequestMapping.RequestParameter
                parameterValues[i] = KotlinClassUtil.changeType(
                    classes[i].kotlin, httpServletRequest.getParameterValues(requestParameterAnnotation.value)
                        ?: emptyArray(), Constants.String.BLANK, this.classProcessor
                )
            } else if (ObjectUtil.isEntity(httpServletRequest, classes[i])) {
                parameterValues[i] = httpServletRequest
            } else if (ObjectUtil.isEntity(httpServletResponse, classes[i])) {
                parameterValues[i] = httpServletResponse
            } else {
                if (KotlinClassUtil.isBaseArray(classes[i].kotlin) || KotlinClassUtil.isSimpleClass(classes[i].kotlin) || KotlinClassUtil.isSimpleArray(classes[i].kotlin)) {
                    parameterValues[i] = KotlinClassUtil.changeType(classes[i].kotlin, emptyArray(), Constants.String.BLANK, this.classProcessor)
                } else if (classes[i].isArray) {
                    val clazz = classes[i].componentType
                    val kClass = clazz.kotlin
                    val objectList = httpServletRequest.parameterMap.toObjectList(kClass, this.classProcessor)
                    if (objectList.isNotEmpty()) {
                        val objectArray = objectList.toArray(kClass)
                        parameterValues[i] = objectArray
                    }
                } else {
                    val instance = classes[i].newInstance()
                    httpServletRequest.parameterMap.toObject(instance, this.classProcessor)
                    parameterValues[i] = instance
                }
            }
        }
        return parameterValues
    }

    /**
     * do annotation bean
     * @param uri
     * @param actionBean
     * @param httpServletRequest
     * @param httpServletResponse
     * @param httpRequestMethod
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws ServletException
     */
    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, ServletException::class, IOException::class)
    private fun doAnnotationAction(uri: String, actionBean: ActionBean, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        if (actionBean !is AnnotationActionBean) {
            logger.error("It is not AnnotationActionBean, actionBean:%s, it is impossible", actionBean)
            return false
        }
        val actionInstance = actionBean.actionObjectBean?.instance
        val parameterMap = httpServletRequest.parameterMap as Map<String, Array<String>>
        val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
        val (normalExecute, needToStaticExecute) = this.getExecuteType(actionForwardBean)
        var path: String = Constants.String.BLANK
        if (normalExecute || needToStaticExecute) {
            if (normalExecute) {
                logger.info("Common bean action (%s) is executing.", actionInstance ?: Constants.String.NULL)
            } else if (needToStaticExecute) {
                logger.info("Need to static execute, first time executing original action")
            }
            val parameterValues = this.annotationActionMethodParameterValues(actionBean, httpServletRequest, httpServletResponse)
            val methodInvokeValue = actionBean.method?.invoke(actionInstance, *parameterValues)
            if (methodInvokeValue != null && methodInvokeValue is String) {
                path = methodInvokeValue.toString()
            } else {
                logger.error("Common bean action (%s) is execute error, method is null or method return value is not String", actionInstance)
            }
        } else {
            logger.info("Static execute, not the first time execute")
        }
        val afterActionInterceptorResult = this.doAfterActionInterceptor(uri, actionBean, httpServletRequest, httpServletResponse)
        if (!afterActionInterceptorResult) {
            return false
        }

        val afterGlobalInterceptorResult = this.doAfterGlobalInterceptor(uri, httpServletRequest, httpServletResponse)
        if (!afterGlobalInterceptorResult) {
            return false
        }

        this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, httpServletRequest, httpServletResponse, true)
        return true
    }

    /**
     * do after action interceptor
     * @param uri
     * @param actionBean
     * @param httpServletRequest
     * @param httpServletResponse
     * @return boolean
     */
    private fun doAfterActionInterceptor(uri: String, actionBean: ActionBean, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Boolean {
        val afterActionBeanInterceptorList = actionBean.afterActionInterceptorBeanList
        val afterActionInterceptorResult = doActionInterceptorBeanList(afterActionBeanInterceptorList, httpServletRequest, httpServletResponse)
        if (afterActionInterceptorResult.type == InterceptorInterface.Result.Type.STOP) {
            logger.info("Stopping through the before action interceptors! Maybe special use, like request forward. The request name:%s", uri)
            return false
        } else if (afterActionInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("Can not through the after action interceptors! The request name:%s", uri)
            return false
        }
        logger.info("Through the after action interceptors! The request name:%s", uri)
        return true
    }

    /**
     * do after global interceptor
     * @param uri
     * @param httpServletRequest
     * @param httpServletResponse
     * @return boolean
     */
    private fun doAfterGlobalInterceptor(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): Boolean {
        val afterGlobalInterceptorBeanIterable = ConfigurationContainer.rootConfigurationContext.afterGlobalInterceptorBeanIterable
        val afterGlobalInterceptorResult = doGlobalInterceptorBeanIterable(afterGlobalInterceptorBeanIterable, httpServletRequest, httpServletResponse)
        if (afterGlobalInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("Can not through the after global interceptors! The request name:%s", uri)
            return false
        }
        logger.info("Through the after global interceptors! The request name:%s", uri)
        return true
    }

    /**
     * get execute type
     * @param actionForwardBean
     * @return Pair<Boolean, Boolean>
     */
    private fun getExecuteType(actionForwardBean: ActionForwardBean?): Pair<Boolean, Boolean> {
        var normalExecute = true//default normal execute
        var needToStaticExecute = false
        if (actionForwardBean != null) {//static file page
            normalExecute = false
            val staticFilePathKey = actionForwardBean.staticFilePath
            if (!StaticFilePathUtil.isContainsStaticFilePath(staticFilePathKey)) {
                needToStaticExecute = true
            }
        }
        return normalExecute to needToStaticExecute
    }

    /**
     * do forward
     * @param normalExecute
     * @param needToStaticExecute
     * @param actionForwardBean
     * @param path
     * @param httpServletRequest
     * @param httpServletResponse
     * @param annotationBeanExecute
     * @throws IOException
     * @throws ServletException
     */
    @Throws(ServletException::class, IOException::class)
    private fun doForward(normalExecute: Boolean, needToStaticExecute: Boolean, actionForwardBean: ActionForwardBean?, path: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, annotationBeanExecute: Boolean) {
        var realPath = path
        if (!normalExecute && !needToStaticExecute) {
            val staticFilePath = actionForwardBean!!.staticFilePath
            logger.info("Send redirect to static file path:%s", staticFilePath)
            val requestDispatcher = httpServletRequest.getRequestDispatcher(staticFilePath)
            requestDispatcher.forward(httpServletRequest, httpServletResponse)
        } else {
            if (realPath.isNotBlank()) {
                realPath = ActionUtil.parsePath(realPath)
                if (normalExecute) {
                    if (annotationBeanExecute) {
                        logger.info("Annotation bean action executed forward path:%s", realPath)
                    } else {
                        logger.info("Normal executed forward path:%s", realPath)
                    }
                    val requestDispatcher = httpServletRequest.getRequestDispatcher(realPath)
                    requestDispatcher.forward(httpServletRequest, httpServletResponse)
                } else if (needToStaticExecute) {
                    val staticFilePath = actionForwardBean!!.staticFilePath
                    val configurationContext = ConfigurationContainer.rootConfigurationContext
                    if (StaticFilePathUtil.staticize(realPath, configurationContext.projectRealPath + staticFilePath, httpServletRequest, httpServletResponse)) {
                        logger.info("Static executed success, redirect static file:%s", staticFilePath)
                        val requestDispatcher = httpServletRequest.getRequestDispatcher(staticFilePath)
                        requestDispatcher.forward(httpServletRequest, httpServletResponse)
                        StaticFilePathUtil.addStaticFilePath(staticFilePath, staticFilePath)
                    } else {
                        logger.info("Static executed failure, file:%s", staticFilePath)
                    }
                }
            } else {
                if (annotationBeanExecute) {
                    logger.info("May be ajax use if not please config the entity page with String type.")
                } else {
                    logger.info("System can not find the path:%s", realPath)
                }
            }
        }
    }

    /**
     * do global interceptor list,include global(before,after)
     * @param globalInterceptorBeanIterable
     * @param httpServletRequest
     * @param httpServletResponse
     * @return InterceptorInterface.Result
     */
    private fun doGlobalInterceptorBeanIterable(globalInterceptorBeanIterable: Iterable<GlobalInterceptorBean>, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): InterceptorInterface.Result {
        try {
            for (globalInterceptorBean in globalInterceptorBeanIterable) {
                val result = globalInterceptorBean.interceptorInstance.intercept(httpServletRequest, httpServletResponse)
                val sign = result.type
                logger.info("Global interceptor, through:%s, interceptor:%s", sign, globalInterceptorBean)
                if (sign != InterceptorInterface.Result.Type.NEXT) {
                    return result
                }
            }
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
            return InterceptorInterface.Result(InterceptorInterface.Result.Type.ERROR)
        }
        return InterceptorInterface.Result()
    }

    /**
     * do action bean interceptor list,include action(before,action)
     * @param actionInterceptorBeanList
     * @param httpServletRequest
     * @param httpServletResponse
     * @return InterceptorInterface.Result
     */
    private fun doActionInterceptorBeanList(actionInterceptorBeanList: List<ActionInterceptorBean>, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse): InterceptorInterface.Result {
        try {
            for (actionInterceptorBean in actionInterceptorBeanList) {
                val actionInterceptor = actionInterceptorBean.interceptorInstance
                val result = actionInterceptor.intercept(httpServletRequest, httpServletResponse)
                val sign = result.type
                logger.info("Action interceptor, through:%s, interceptor:%s", sign, actionInterceptor)
                if (sign != InterceptorInterface.Result.Type.NEXT) {
                    return result
                }
            }
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
            return InterceptorInterface.Result(InterceptorInterface.Result.Type.ERROR)
        }
        return InterceptorInterface.Result()
    }

    interface Lifecycle {
        /**
         * on request
         */
        fun onRequest(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod)

        /**
         * on response
         */
        fun onResponse(uri: String, httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod)
    }
}
