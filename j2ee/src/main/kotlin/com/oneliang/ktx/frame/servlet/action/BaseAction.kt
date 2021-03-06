package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.StaticVar
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.i18n.MessageContext
import com.oneliang.ktx.frame.servlet.ActionUtil
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.upload.FileUpload
import com.oneliang.ktx.util.upload.FileUploadResult
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class BaseAction {
    companion object {
        private val logger = LoggerManager.getLogger(BaseAction::class)
    }

    protected val projectRealPath = ConfigurationContainer.rootConfigurationContext.projectRealPath

    /**
     * base path
     * @return String
     */
    protected val basePath: String
        get() {
            val servletRequest = ActionUtil.servletRequest
            val httpServletRequest = servletRequest as HttpServletRequest
            val contextPath = httpServletRequest.contextPath
            return servletRequest.getScheme() + Constants.Symbol.COLON + Constants.Symbol.SLASH_LEFT + Constants.Symbol.SLASH_LEFT + servletRequest.getServerName() + Constants.Symbol.COLON + servletRequest.getServerPort() + contextPath
        }

    /**
     * get session id
     * @return String
     */
    protected val sessionId: String
        get() {
            val httpRequest = ActionUtil.servletRequest as HttpServletRequest
            val httpSession = httpRequest.session
            return httpSession.id
        }

    /**
     * get request ip
     * @return String
     */
    protected val requestIp: String
        get() {
            val servletRequest = ActionUtil.servletRequest
            return servletRequest.remoteAddr
        }

    /**
     * get request uri
     * @return String
     */
    protected val requestUri: String
        get() {
            val servletRequest = ActionUtil.servletRequest
            val httpServletRequest = servletRequest as HttpServletRequest
            var uri = httpServletRequest.requestURI
            val front = httpServletRequest.contextPath.length
            uri = uri.substring(front, uri.length)
            return uri
        }

    /**
     * get byte array from input stream
     */
    protected fun getByteArrayFromInputStream(checkContentType: Boolean = true): ByteArray {
        val copyBlock: (servletRequest: ServletRequest, byteArrayOutputStream: ByteArrayOutputStream) -> Unit = { servletRequest, byteArrayOutputStream ->
            try {
                val inputStream = servletRequest.inputStream
                inputStream.copyTo(byteArrayOutputStream)
                byteArrayOutputStream.close()
            } catch (e: Throwable) {
                logger.error(Constants.String.EXCEPTION, e)
            }
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        val servletRequest = ActionUtil.servletRequest
        val contentType = servletRequest.contentType
        if (checkContentType && !contentType.isNullOrBlank()) {
            if (contentType.indexOf(Constants.Http.ContentType.APPLICATION_OCTET_STREAM) >= 0 || contentType.indexOf(Constants.Http.ContentType.BINARY_OCTET_STREAM) >= 0) {
                copyBlock(servletRequest, byteArrayOutputStream)
            } else {
                //nothing to do, byteArrayOutputStream will empty
            }
        } else {
            copyBlock(servletRequest, byteArrayOutputStream)
        }
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * get byte array from input stream most use for binary stream
     */
    protected val byteArrayFromInputStream: ByteArray
        @Throws(Exception::class)
        get() = getByteArrayFromInputStream(true)

    /**
     * Method: set the instance object to the request
     * @param <T>
     * @param key
     * @param value
    </T> */
    protected fun <T : Any> setObjectToRequest(key: String, value: T) {
        val servletRequest = ActionUtil.servletRequest
        servletRequest.setAttribute(key, value)
    }

    /**
     * Method: set the instance object to the session
     * @param <T>
     * @param key
     * @param value
    </T> */
    protected fun <T : Any> setObjectToSession(key: String, value: T) {
        val servletRequest = ActionUtil.servletRequest
        (servletRequest as HttpServletRequest).session.setAttribute(key, value)
    }

    /**
     * Method: get object from request attribute
     * @param key
     * @return Object
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> getObjectFromRequest(key: String): T? {
        val servletRequest = ActionUtil.servletRequest
        return servletRequest.getAttribute(key) as T?
    }

    /**
     * Method: remove object from session attribute
     * @param key
     */
    protected fun removeObjectFromRequest(key: String) {
        val servletRequest = ActionUtil.servletRequest
        servletRequest.removeAttribute(key)
    }

    /**
     * Method: remove object from session attribute
     * @param key
     */
    protected fun removeObjectFromSession(key: String) {
        val servletRequest = ActionUtil.servletRequest
        (servletRequest as HttpServletRequest).session.removeAttribute(key)
    }

    /**
     * remove current session
     */
    protected fun removeSession() {
        val httpRequest = ActionUtil.servletRequest as HttpServletRequest
        httpRequest.session.invalidate()
    }

    /**
     * Method: get the instance object to the session by key
     * @param key
     * @return Object
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> getObjectFromSession(key: String): T? {
        val servletRequest = ActionUtil.servletRequest
        return (servletRequest as HttpServletRequest).session.getAttribute(key) as T?
    }

    /**
     * Method: get the parameter from request
     * @param parameter
     * @return String
     */
    protected fun getParameter(parameter: String): String {
        val servletRequest = ActionUtil.servletRequest
        return servletRequest.getParameter(parameter).nullToBlank()
    }

    /**
     * Method:get the parameter values from request
     * @param parameter
     * @return String[]
     */
    protected fun getParameterValues(parameter: String): Array<String> {
        val servletRequest = ActionUtil.servletRequest
        return servletRequest.getParameterValues(parameter) ?: emptyArray()
    }

    /**
     * get header
     * @param name
     * @return String
     */
    protected fun getHeader(name: String): String {
        val servletRequest = ActionUtil.servletRequest
        val httpServletRequest = servletRequest as HttpServletRequest
        return httpServletRequest.getHeader(name).nullToBlank()
    }

    /**
     * forward
     * @param path
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun forward(path: String) {
        val servletRequest = ActionUtil.servletRequest
        val servletResponse = ActionUtil.servletResponse
        servletRequest.getRequestDispatcher(path).forward(servletRequest, servletResponse)
    }

    /**
     * write
     * @param string
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun write(string: String) {
        val response = ActionUtil.servletResponse
        response.writer.write(string)
    }

    /**
     * write
     * @param byteArray
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun write(byteArray: ByteArray) {
        val response = ActionUtil.servletResponse
        response.outputStream.write(byteArray)
    }

    /**
     * get message
     * @param key
     * @return String
     */
    protected fun getMessage(key: String): String? {
        val locale = Locale.getDefault().toString()
        return getMessage(key, locale)
    }

    /**
     * get message
     * @param key
     * @param locale
     * @return String
     */
    protected fun getMessage(key: String, locale: String): String {
        val properties = MessageContext.getMessageProperties(locale)
        return properties?.getProperty(key).nullToBlank()
    }

    /**
     * upload for form
     * @param filePath
     * @param saveFilenames
     * @return List<FileUploadResult>
     * @throws Exception
    </FileUploadResult> */
    @Throws(Exception::class)
    protected fun uploadForForm(filePath: String, saveFilenames: Array<String>): List<FileUploadResult>? {
        var tempFilePath = filePath
        val servletRequest = ActionUtil.servletRequest
        val response = ActionUtil.servletResponse
        response.contentType = Constants.Http.ContentType.TEXT_PLAIN
        // get content type of client request
        val contentType = servletRequest.contentType
        var fileUploadResultList: List<FileUploadResult>? = null
        if (contentType != null) {
            val fileUpload = FileUpload()
            if (tempFilePath.isBlank()) {
                tempFilePath = StaticVar.UPLOAD_FOLDER
            }
            //filePath=new File(filePath).getAbsolutePath()
            FileUtil.createDirectory(tempFilePath)
            fileUpload.saveFilePath = tempFilePath
            // make sure content type is multipart/form-data,form file use
            if (contentType.indexOf(Constants.Http.ContentType.MULTIPART_FORM_DATA) >= 0) {
                fileUploadResultList = fileUpload.upload(servletRequest.inputStream, servletRequest.contentLength, saveFilenames)
            }
        }
        return fileUploadResultList
    }

    /**
     * upload for stream
     * @param filePath
     * @param filename
     * @return FileUploadResult
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun uploadForStream(filePath: String, filename: String): FileUploadResult? {
        var tempFilePath = filePath
        val servletRequest = ActionUtil.servletRequest
        val response = ActionUtil.servletResponse
        response.contentType = Constants.Http.ContentType.TEXT_PLAIN
        // get content type of client request
        val contentType = servletRequest.contentType
        var fileUploadResult: FileUploadResult? = null
        if (contentType != null) {
            val fileUpload = FileUpload()
            if (tempFilePath.isBlank()) {
                tempFilePath = StaticVar.UPLOAD_FOLDER
            }
            FileUtil.createDirectory(tempFilePath)
            fileUpload.saveFilePath = tempFilePath
            // make sure content type is application/octet-stream or binary/octet-stream,flash jpeg picture use or file upload use
            if (contentType.indexOf(Constants.Http.ContentType.APPLICATION_OCTET_STREAM) >= 0 || contentType.indexOf(Constants.Http.ContentType.BINARY_OCTET_STREAM) >= 0) {
                if (filename.isNotBlank()) {
                    fileUploadResult = fileUpload.upload(servletRequest.inputStream, filename)
                }
            }
        }
        return fileUploadResult
    }

    /**
     * download file
     * @param fullPathName
     * @throws Exception
     */
    @Throws(Exception::class)
    protected fun download(fullPathName: String, newFilename: String): Boolean {
        var tempNewFilename = newFilename
        val response = ActionUtil.servletResponse
        var result = false
        val file = File(fullPathName)
        response.contentType = Constants.Http.ContentType.APPLICATION_X_DOWNLOAD
        if (tempNewFilename.isBlank()) {
            tempNewFilename = String(file.name.toByteArray(Charset.forName(Constants.Encoding.GB2312)), Charset.forName(Constants.Encoding.ISO88591))
        }
        (response as HttpServletResponse).addHeader(Constants.Http.HeaderKey.CONTENT_DISPOSITION, "attachment;filename=$tempNewFilename")
        response.setContentLength(file.length().toInt())
        val outputStream = response.getOutputStream()
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(fullPathName)
            val buffer = ByteArray(Constants.Capacity.BYTES_PER_KB)
            var length = inputStream.read(buffer, 0, buffer.size)
            while (length != -1) {
                outputStream.write(buffer, 0, length)
                outputStream.flush()
                length = inputStream.read(buffer, 0, buffer.size)
            }
            outputStream.flush()
            result = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream.close()
        }
        return result
    }
}

fun <T : Any> BaseAction.setObjectToRequest(block: () -> Array<Pair<String, T>>) {
    val valueArray = block()
    val servletRequest = ActionUtil.servletRequest
    for (pair in valueArray) {
        servletRequest.setAttribute(pair.first, pair.second)
    }
}

fun <T : Any> BaseAction.setObjectToSession(block: () -> Array<Pair<String, T>>) {
    val valueArray = block()
    val servletRequest = ActionUtil.servletRequest
    val session = (servletRequest as HttpServletRequest).session
    for (pair in valueArray) {
        session.setAttribute(pair.first, pair.second)
    }
}
