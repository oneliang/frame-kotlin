package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.parseRegexGroup
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.*
import javax.servlet.*
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

object ActionUtil {

    private val logger = LoggerManager.getLogger(ActionUtil::class)

    private const val REGEX = "\\{([\\w]*)\\}"

    private val servletBeanThreadLocal = ThreadLocal<ServletBean>()

    /**
     * get servlet bean
     * @return ServletBean
     */
    /**
     * set servlet bean
     * @param servletBean
     */
    internal var servletBean: ServletBean?
        get() = servletBeanThreadLocal.get()
        set(servletBean) = servletBeanThreadLocal.set(servletBean)

    /**
     * get servlet context
     * @return ServletContext
     */
    val servletContext: ServletContext
        get() = servletBeanThreadLocal.get().servletContext!!

    /**
     * get servlet request
     * @return ServletRequest
     */
    val servletRequest: ServletRequest
        get() = servletBeanThreadLocal.get().servletRequest!!

    /**
     * get servlet response
     * @return ServletResponse
     */
    val servletResponse: ServletResponse
        get() = servletBeanThreadLocal.get().servletResponse!!

    /**
     * parse forward path and replace the parameter value
     * all by request.setAttribute() and request.getAttribute()
     * @param path
     * @return path
     */
    internal fun parsePath(path: String): String {
        var resultPath = path
        val request = servletRequest
        val attributeList = resultPath.parseRegexGroup(REGEX)
        for (attribute in attributeList) {
            val attributeValue = request.getAttribute(attribute)
            resultPath = resultPath.replaceFirst(REGEX.toRegex(), if (attributeValue == null) Constants.String.BLANK else attributeValue!!.toString())
        }
        return resultPath
    }

    /**
     * include jsp,for servlet(ActionListener) use.
     * @param jspUriPath
     * @return ByteArrayOutputStream
     * @throws IOException
     * @throws ServletException
     */
    @Throws(ServletException::class, IOException::class)
    fun includeJsp(jspUriPath: String): ByteArrayOutputStream {
        val request = servletRequest
        val response = servletResponse
        return includeJsp(jspUriPath, request, response)
    }

    /**
     * include jsp,and return the byte array output stream
     * @param jspUriPath
     * @param servletRequest
     * @param servletResponse
     * @return ByteArrayOutputStream
     * @throws IOException
     * @throws ServletException
     */
    @Throws(ServletException::class, IOException::class)
    fun includeJsp(jspUriPath: String, servletRequest: ServletRequest, servletResponse: ServletResponse): ByteArrayOutputStream {
        val requestDispatcher = servletRequest.getRequestDispatcher(jspUriPath)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val servletOutputStream = object : ServletOutputStream() {
            override fun write(b: ByteArray, off: Int, len: Int) {
                byteArrayOutputStream.write(b, off, len)
            }

            override fun write(b: Int) {
                byteArrayOutputStream.write(b)
            }

            override fun isReady(): Boolean {
                return true
            }

            override fun setWriteListener(writeListener: WriteListener?) {
            }
        }
        val printWriter = PrintWriter(OutputStreamWriter(byteArrayOutputStream))
        val httpServletResponse = object : HttpServletResponseWrapper(servletResponse as HttpServletResponse) {
            override fun getOutputStream(): ServletOutputStream {
                return servletOutputStream
            }

            override fun getWriter(): PrintWriter {
                return printWriter
            }
//            val outputStream: ServletOutputStream
//                get() = servletOutputStream
//            val writer: PrintWriter
//                get() = printWriter
        }
        requestDispatcher.include(servletRequest, httpServletResponse)
        printWriter.flush()
        return byteArrayOutputStream
    }

    /**
     * include jsp and save,for servlet(ActionListener) use.
     * @param jspUriPath
     * @param saveFullFilename
     * @return boolean
     */
    fun includeJspAndSave(jspUriPath: String, saveFullFilename: String): Boolean {
        val request = servletRequest
        val response = servletResponse
        return includeJspAndSave(jspUriPath, saveFullFilename, request, response)
    }

    /**
     * include jsp and save
     * @param jspUriPath
     * @param saveFullFilename
     * @param servletRequest
     * @param servletResponse
     * @return boolean
     */
    fun includeJspAndSave(jspUriPath: String, saveFullFilename: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Boolean {
        var result = false
        try {
            val byteArrayOutputStream = includeJsp(jspUriPath, servletRequest, servletResponse)
            FileUtil.createFile(saveFullFilename)
            val fileOutputStream = FileOutputStream(saveFullFilename)
            val bufferedWriter = BufferedWriter(OutputStreamWriter(fileOutputStream, Constants.Encoding.UTF8))
            bufferedWriter.write(String(byteArrayOutputStream.toByteArray()))
            bufferedWriter.flush()
            bufferedWriter.close()
            result = true
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return result
    }

    internal class ServletBean : Serializable {

        /**
         * @return the servletContext
         */
        /**
         * @param servletContext the servletContext to set
         */
        var servletContext: ServletContext? = null
        /**
         * @return the servletRequest
         */
        /**
         * @param servletRequest the servletRequest to set
         */
        var servletRequest: ServletRequest? = null
        /**
         * @return the servletResponse
         */
        /**
         * @param servletResponse the servletResponse to set
         */
        var servletResponse: ServletResponse? = null
    }
}