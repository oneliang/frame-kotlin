package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.servlet.ActionUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.io.FileInputStream
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class AbstractDownloadFileAction : BaseAction() {

    companion object {
        private val logger = LoggerManager.getLogger(AbstractDownloadFileAction::class)
    }

    /**
     * download file
     * @param filename
     * @return boolean
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun download(filename: String): Boolean {
        val request = ActionUtil.servletRequest as HttpServletRequest
        val response = ActionUtil.servletResponse as HttpServletResponse
        return this.download(request, response, filename)
    }

    /**
     * download file
     * @param request
     * @param response
     * @param fullFilename
     * @throws ActionExecuteException
     */
    @Throws(ActionExecuteException::class)
    protected fun download(request: ServletRequest, response: ServletResponse, fullFilename: String): Boolean {
        logger.info("download full filename:%s", fullFilename)
        response.contentType = Constants.Http.ContentType.APPLICATION_X_DOWNLOAD
        try {
            val file = File(fullFilename)
            val newFilename = String(file.name.toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
            (response as HttpServletResponse).addHeader(Constants.Http.HeaderKey.CONTENT_DISPOSITION, "attachment;filename=$newFilename")
            val outputStream = response.getOutputStream()
            outputStream.use {
                val inputStream = FileInputStream(fullFilename)
                inputStream.use {
                    it.copyTo(outputStream)
                }
            }
            return true
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }
    }
}
