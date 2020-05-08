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
    }

    private var classProcessor = KotlinClassUtil.DEFAULT_KOTLIN_CLASS_PROCESSOR

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
    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        //servlet bean
        var servletBean: ActionUtil.ServletBean? = ActionUtil.servletBean
        if (servletBean == null) {
            servletBean = ActionUtil.ServletBean()
            ActionUtil.servletBean = servletBean
        }
        servletBean.servletContext = this.servletContext
        servletBean.servletRequest = request
        servletBean.servletResponse = response
        //execute default service method,distribute doGet or doPost or other http method
        super.service(request, response)
        //servlet bean request and response set null
        servletBean.servletRequest = null
        servletBean.servletResponse = null
    }

    override fun getLastModified(request: HttpServletRequest): Long {
        //uri
        //		String uri=request.getRequestURI()
        //		int front=request.getContextPath().length()
        //		uri=uri.substring(front,uri.length())
        //		return 1368624759725l
        return super.getLastModified(request)
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
     *
     * This method is called when a HTTP head request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doHead(request: HttpServletRequest, response: HttpServletResponse) {
        super.doHead(request, response)
        logRequestForOtherCase(request, response, ActionInterface.HttpRequestMethod.HEAD)
    }

    /**
     * The doTrace method of the servlet. <br></br>
     *
     * This method is called when a HTTP trace request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doTrace(request: HttpServletRequest, response: HttpServletResponse) {
        super.doTrace(request, response)
        logRequestForOtherCase(request, response, ActionInterface.HttpRequestMethod.TRACE)
    }

    /**
     * The doOptions method of the servlet. <br></br>
     *
     * This method is called when a HTTP options request is received.
     *
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
    private fun logRequestForOtherCase(request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        val uri = request.requestURI
        logger.info("It is requesting uri:%s, http method:%s", uri, httpRequestMethod.name)
    }

    /**
     * The doDelete method of the servlet. <br></br>
     *
     * This method is called when a HTTP delete request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doDelete(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.DELETE)
    }

    /**
     * The doGet method of the servlet. <br></br>
     *
     * This method is called when a form has its tag value method equals to get.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.GET)
    }

    /**
     * The doPost method of the servlet. <br></br>
     *
     * This method is called when a form has its tag value method equals to post.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.POST)
    }


    /**
     * The doPut method of the servlet. <br></br>
     *
     * This method is called when a HTTP put request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    @Throws(ServletException::class, IOException::class)
    override fun doPut(request: HttpServletRequest, response: HttpServletResponse) {
        dispatch(request, response, ActionInterface.HttpRequestMethod.PUT)
    }

    /**
     * dispatch http request
     * @param request
     * @param response
     * @param httpRequestMethod
     * @throws ServletException
     * @throws IOException
     */
    @Throws(ServletException::class, IOException::class)
    private fun dispatch(request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod) {
        //uri
        var uri = request.requestURI

        logger.info("It is requesting uri:%s, http method:%s", uri, httpRequestMethod.name)

        val front = request.contextPath.length
        //		int rear=uri.lastIndexOf(StaticVar.DOT)
        //		if(rear>front){
        uri = uri.substring(front, uri.length)
        //		}
        //		uri=uri.substring(front,rear)
        logger.info("The request name is:$uri")

        request.setAttribute(ConstantsAction.RequestKey.KEY_STRING_CURRENT_REQUEST_URI, uri)

        val beforeGlobalInterceptorResult = this.doBeforeGlobalInterceptor(uri, request, response)
        if (!beforeGlobalInterceptorResult) {
            return
        }
        val actionResult = doAction(uri, request, response, httpRequestMethod)
        if (!actionResult) {
            return
        }
    }

    /**
     * Initialization of the servlet. <br></br>
     *
     * @throws ServletException if an error occurs
     */
    @Throws(ServletException::class)
    override fun init() {
        logger.info("System is starting up,listener is initial")
        val classProcessorClassName = getInitParameter(INIT_PARAMETER_CLASS_PROCESSOR)
        if (classProcessorClassName != null && classProcessorClassName.isNotBlank()) {
            try {
                val clazz = Thread.currentThread().contextClassLoader.loadClass(classProcessorClassName)
                if (ObjectUtil.isInterfaceImplement(clazz, KotlinClassUtil.KotlinClassProcessor::class.java)) {
                    this.classProcessor = clazz.newInstance() as KotlinClassUtil.KotlinClassProcessor
                }
            } catch (e: Throwable) {
                logger.error(Constants.Base.EXCEPTION, e)
            }
        }
    }

    /**
     * do before global interceptor
     * @param uri
     * @param request
     * @param response
     * @return boolean
     */
    private fun doBeforeGlobalInterceptor(uri: String, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        //global interceptor doIntercept
        val beforeGlobalInterceptorList = ConfigurationContainer.rootConfigurationContext.beforeGlobalInterceptorList
        val beforeGlobalInterceptorResult = doGlobalInterceptorList(beforeGlobalInterceptorList, request, response)

        //through the interceptor
        if (beforeGlobalInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("The request name:%s. Can not through the before global interceptors", uri)
            response.status = Constants.Http.StatusCode.FORBIDDEN
            response.outputStream.write(beforeGlobalInterceptorResult.message)
            return false
        } else if (beforeGlobalInterceptorResult.type == InterceptorInterface.Result.Type.CUSTOM) {
            logger.info("The request name:%s. Use CUSTOM to through the before global interceptors", uri)
            return false
        }
        logger.info("Through the before global interceptors!")
        return true
    }

    /**
     * do before action interceptor
     * @param uri
     * @param actionBean
     * @param request
     * @param response
     * @return boolean
     */
    private fun doBeforeActionInterceptor(uri: String, actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        //action interceptor doIntercept
        val beforeActionBeanInterceptorList = actionBean.beforeActionInterceptorBeanList
        val beforeActionInterceptorResult = doActionInterceptorBeanList(beforeActionBeanInterceptorList, request, response)
        if (beforeActionInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("The request name:%s. Can not through the before action interceptors", uri)
            response.status = Constants.Http.StatusCode.FORBIDDEN
            response.outputStream.write(beforeActionInterceptorResult.message)
            return false
        } else if (beforeActionInterceptorResult.type == InterceptorInterface.Result.Type.CUSTOM) {
            logger.info("The request name:%s. Use CUSTOM to through the before action interceptors", uri)
            return false
        }
        logger.info("Through the before action interceptors!")
        return true
    }

    /**
     * do action
     * @param uri
     * @param request
     * @param response
     * @return boolean
     * @throws IOException
     * @throws ServletException
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class, ServletException::class, IOException::class)
    private fun doAction(uri: String, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        val actionBeanList = ConfigurationContainer.rootConfigurationContext.findActionBeanList(uri)
        if (actionBeanList.isNullOrEmpty()) {
            logger.info("The request name:$uri. It does not exist, please config the name and entity class")
            response.status = Constants.Http.StatusCode.NOT_FOUND
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
            logger.info("The request name:$uri. Method not allowed, http request method:$httpRequestMethod")
            response.status = Constants.Http.StatusCode.METHOD_NOT_ALLOWED
            return false
        }
        val beforeActionInterceptorResult = this.doBeforeActionInterceptor(uri, actionBean, request, response)
        if (!beforeActionInterceptorResult) {
            return false
        }
        val actionInstance = actionBean.actionInstance
        return try {
            if (actionInstance is ActionInterface) {
                doAction(uri, actionBean, request, response, httpRequestMethod)
            } else {
                doAnnotationAction(uri, actionBean, request, response, httpRequestMethod)
            }
        } catch (e: Throwable) {
            logger.error(Constants.Base.EXCEPTION, e)
            logger.info("The request name:$uri. Action or page does not exist")
            val exceptionPath = ConfigurationContainer.rootConfigurationContext.globalExceptionForwardPath.nullToBlank()
            if (exceptionPath.isNotBlank()) {
                logger.info("Forward to exception path:$exceptionPath")
                request.setAttribute(Constants.Base.EXCEPTION, e)
                val requestDispatcher = request.getRequestDispatcher(exceptionPath)
                requestDispatcher.forward(request, response)
            } else {
                logger.info("System can not find the exception path.Please config the global exception forward path.")
                response.status = Constants.Http.StatusCode.INTERNAL_SERVER_ERROR
            }
            false
        }
    }

    /**
     * do action
     * @param uri
     * @param actionBean
     * @param request
     * @param response
     * @return boolean
     */
    @Throws(ActionExecuteException::class, ServletException::class, IOException::class)
    private fun doAction(uri: String, actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        val actionInstance = actionBean.actionInstance
        if (actionInstance !is ActionInterface) {
            logger.error("It is not ActionInterface, actionBean:$actionBean, it is impossible")
            return false
        }
        logger.info("Action implements ($actionInstance) is executing")
        //judge is it contain static file page
        val parameterMap = request.parameterMap as Map<String, Array<String>>
        val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
        val (normalExecute, needToStaticExecute) = this.getExecuteType(actionForwardBean)
        val forward = if (normalExecute || needToStaticExecute) {
            if (normalExecute) {
                logger.info("Normal executing")
            } else if (needToStaticExecute) {
                logger.info("Need to static execute,first time executing original action")
            }
            actionInstance.execute(request, response)
        } else {
            logger.info("Static execute,not the first time execute")
            actionForwardBean!!.name
        }
        val afterActionInterceptorResult = this.doAfterActionInterceptor(actionBean, request, response)
        if (!afterActionInterceptorResult) {
            return false
        }

        val afterGlobalInterceptorResult = this.doAfterGlobalInterceptor(uri, request, response)
        if (!afterGlobalInterceptorResult) {
            return false
        }

        if (forward.isNotBlank()) {
            var path = actionBean.findForwardPath(forward)
            if (path.isNotBlank()) {
                logger.info("The forward name in configFile is--:actionPath:" + actionBean.path + "--forward:" + forward + "--path:" + path)
            } else {
                path = ConfigurationContainer.rootConfigurationContext.findGlobalForwardPath(forward)
                logger.info("The forward name in global forward configFile is--:forward:$forward--path:$path")
            }
            this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, request, response, false)
        } else {
            logger.info("The forward name--:$forward does not exist,may be ajax use if not please config the name and entity page or class")
        }
        return true
    }

    /**
     * @param annotationActionBean
     * @param request
     * @param response
     * @return Object[]
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class)
    private fun annotationActionMethodParameterValues(annotationActionBean: AnnotationActionBean, request: HttpServletRequest, response: HttpServletResponse): Array<Any?> {
        val annotationActionBeanMethod = annotationActionBean.method!!
        val classes = annotationActionBeanMethod.parameterTypes
        val parameterValues = arrayOfNulls<Any>(classes.size)
        val annotations = annotationActionBeanMethod.parameterAnnotations
        for (i in annotations.indices) {
            if (annotations[i].isNotEmpty() && annotations[i][0] is Action.RequestMapping.RequestParameter) {
                val requestParameterAnnotation = annotations[i][0] as Action.RequestMapping.RequestParameter
                parameterValues[i] = KotlinClassUtil.changeType(classes[i].kotlin, request.getParameterValues(requestParameterAnnotation.value)
                        ?: emptyArray(), Constants.String.BLANK, this.classProcessor)
            } else if (ObjectUtil.isEntity(request, classes[i])) {
                parameterValues[i] = request
            } else if (ObjectUtil.isEntity(response, classes[i])) {
                parameterValues[i] = response
            } else {
                if (KotlinClassUtil.isBaseArray(classes[i].kotlin) || KotlinClassUtil.isSimpleClass(classes[i].kotlin) || KotlinClassUtil.isSimpleArray(classes[i].kotlin)) {
                    parameterValues[i] = KotlinClassUtil.changeType(classes[i].kotlin, emptyArray(), Constants.String.BLANK, this.classProcessor)
                } else if (classes[i].isArray) {
                    val clazz = classes[i].componentType
                    val kClass = clazz.kotlin
                    val objectList = request.parameterMap.toObjectList(kClass, this.classProcessor)
                    if (objectList.isNotEmpty()) {
                        val objectArray = objectList.toArray(kClass)
                        parameterValues[i] = objectArray
                    }
                } else {
                    val instance = classes[i].newInstance()
                    request.parameterMap.toObject(instance, this.classProcessor)
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
     * @param request
     * @param response
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws ServletException
     */
    @Throws(IllegalArgumentException::class, InstantiationException::class, IllegalAccessException::class, InvocationTargetException::class, ServletException::class, IOException::class)
    private fun doAnnotationAction(uri: String, actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse, httpRequestMethod: ActionInterface.HttpRequestMethod): Boolean {
        if (actionBean !is AnnotationActionBean) {
            logger.error("It is not AnnotationActionBean, actionBean:$actionBean, it is impossible")
            return false
        }
        val actionInstance = actionBean.actionInstance
        val parameterMap = request.parameterMap as Map<String, Array<String>>
        val actionForwardBean = actionBean.findActionForwardBeanByStaticParameter(parameterMap)
        val (normalExecute, needToStaticExecute) = this.getExecuteType(actionForwardBean)
        var path: String = Constants.String.BLANK
        if (normalExecute || needToStaticExecute) {
            if (normalExecute) {
                logger.info("Common bean action (%s) is executing.", actionInstance ?: Constants.String.NULL)
            } else if (needToStaticExecute) {
                logger.info("Need to static execute,first time executing original action")
            }
            val parameterValues = this.annotationActionMethodParameterValues(actionBean, request, response)
            val methodInvokeValue = actionBean.method?.invoke(actionInstance, *parameterValues)
            if (methodInvokeValue != null && methodInvokeValue is String) {
                path = methodInvokeValue.toString()
            } else {
                logger.error("Common bean action $actionInstance is execute error, method is null or method return value is not String")
            }
        } else {
            logger.info("Static execute,not the first time execute")
        }
        val afterActionInterceptorResult = this.doAfterActionInterceptor(actionBean, request, response)
        if (!afterActionInterceptorResult) {
            return false
        }

        val afterGlobalInterceptorResult = this.doAfterGlobalInterceptor(uri, request, response)
        if (!afterGlobalInterceptorResult) {
            return false
        }

        this.doForward(normalExecute, needToStaticExecute, actionForwardBean, path, request, response, true)
        return true
    }

    /**
     * do after action interceptor
     * @param actionBean
     * @param request
     * @param response
     * @return boolean
     */
    private fun doAfterActionInterceptor(actionBean: ActionBean, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val afterActionBeanInterceptorList = actionBean.afterActionInterceptorBeanList
        val afterActionInterceptorResult = doActionInterceptorBeanList(afterActionBeanInterceptorList, request, response)
        if (afterActionInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("Can not through the after action interceptors")
            return false
        } else if (afterActionInterceptorResult.type == InterceptorInterface.Result.Type.CUSTOM) {
            logger.error("Can not through the after action interceptors, not support for CUSTOM yet")
            return false
        }
        logger.info("Through the after action interceptors!")
        return true
    }

    /**
     * do after global interceptor
     * @param uri
     * @param request
     * @param response
     * @return boolean
     */
    private fun doAfterGlobalInterceptor(uri: String, request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val afterGlobalInterceptorList = ConfigurationContainer.rootConfigurationContext.afterGlobalInterceptorList
        val afterGlobalInterceptorResult = doGlobalInterceptorList(afterGlobalInterceptorList, request, response)
        if (afterGlobalInterceptorResult.type == InterceptorInterface.Result.Type.ERROR) {
            logger.error("The request name:%s. Can not through the after global interceptors", uri)
            return false
        } else if (afterGlobalInterceptorResult.type == InterceptorInterface.Result.Type.CUSTOM) {
            logger.error("The request name:%s. Can not through the after global interceptors, not support for CUSTOM yet", uri)
            return false
        }
        logger.info("Through the after global interceptors!")
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
     * @param request
     * @param response
     * @param annotationBeanExecute
     * @throws IOException
     * @throws ServletException
     */
    @Throws(ServletException::class, IOException::class)
    private fun doForward(normalExecute: Boolean, needToStaticExecute: Boolean, actionForwardBean: ActionForwardBean?, path: String, request: HttpServletRequest, response: HttpServletResponse, annotationBeanExecute: Boolean) {
        var realPath = path
        if (!normalExecute && !needToStaticExecute) {
            val staticFilePath = actionForwardBean!!.staticFilePath
            logger.info("Send redirect to static file path:$staticFilePath")
            val requestDispatcher = request.getRequestDispatcher(staticFilePath)
            requestDispatcher.forward(request, response)
        } else {
            if (realPath.isNotBlank()) {
                realPath = ActionUtil.parsePath(realPath)
                if (normalExecute) {
                    if (annotationBeanExecute) {
                        logger.info("Annotation bean action executed forward path:$realPath")
                    } else {
                        logger.info("Normal executed forward path:$realPath")
                    }
                    val requestDispatcher = request.getRequestDispatcher(realPath)
                    requestDispatcher.forward(request, response)
                } else if (needToStaticExecute) {
                    val staticFilePath = actionForwardBean!!.staticFilePath
                    val configurationContext = ConfigurationContainer.rootConfigurationContext
                    if (StaticFilePathUtil.staticize(realPath, configurationContext.projectRealPath + staticFilePath, request, response)) {
                        logger.info("Static executed success,redirect static file:$staticFilePath")
                        val requestDispatcher = request.getRequestDispatcher(staticFilePath)
                        requestDispatcher.forward(request, response)
                        StaticFilePathUtil.addStaticFilePath(staticFilePath, staticFilePath)
                    } else {
                        logger.info("Static executed failure,file:$staticFilePath")
                    }
                }
            } else {
                if (annotationBeanExecute) {
                    logger.info("May be ajax use if not please config the entity page with String type.")
                } else {
                    logger.info("System can not find the path:$realPath")
                }
            }
        }
    }

    /**
     * do global interceptor list,include global(before,after)
     * @param interceptorList
     * @param request
     * @param response
     * @return InterceptorInterface.Result
     */
    private fun doGlobalInterceptorList(interceptorList: List<InterceptorInterface>, request: HttpServletRequest, response: HttpServletResponse): InterceptorInterface.Result {
        try {
            for (globalInterceptor in interceptorList) {
                val result = globalInterceptor.intercept(request, response)
                val sign = result.type
                logger.info("Global interceptor, through:%s, interceptor:%s", sign, globalInterceptor)
                if (sign != InterceptorInterface.Result.Type.NEXT) {
                    return result
                }
            }
        } catch (e: Throwable) {
            logger.error(Constants.Base.EXCEPTION, e)
            return InterceptorInterface.Result(InterceptorInterface.Result.Type.ERROR)
        }
        return InterceptorInterface.Result()
    }

    /**
     * do action bean interceptor list,include action(before,action)
     * @param actionInterceptorBeanList
     * @param request
     * @param response
     * @return InterceptorInterface.Result
     */
    private fun doActionInterceptorBeanList(actionInterceptorBeanList: List<ActionInterceptorBean>, request: HttpServletRequest, response: HttpServletResponse): InterceptorInterface.Result {
        try {
            for (actionInterceptorBean in actionInterceptorBeanList) {
                val actionInterceptor = actionInterceptorBean.interceptorInstance
                val result = actionInterceptor.intercept(request, response)
                val sign = result.type
                logger.info("Action interceptor, through:%s, interceptor:%s", sign, actionInterceptor)
                if (sign != InterceptorInterface.Result.Type.NEXT) {
                    return result
                }
            }
        } catch (e: Throwable) {
            logger.error(Constants.Base.EXCEPTION, e)
            return InterceptorInterface.Result(InterceptorInterface.Result.Type.ERROR)
        }
        return InterceptorInterface.Result()
    }
}
