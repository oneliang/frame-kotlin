package com.oneliang.ktx.frame.servlet.action

import com.oneliang.ktx.Constants
import com.oneliang.ktx.StaticVar
import com.oneliang.ktx.frame.servlet.ActionUtil
import com.oneliang.ktx.util.upload.FileUpload
import com.oneliang.ktx.util.upload.FileUploadResult
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

abstract class AbstractUploadFileAction : BaseAction() {

    /**
     * file upload from request.getInputStream(),for inputStream submit
     * @param request
     * @param response
     * @param fileFullName
     * @return List<FileUploadResult>
     * @throws ActionExecuteException
    </FileUploadResult> */
    @Throws(ActionExecuteException::class)
    protected fun upload(request: ServletRequest = ActionUtil.servletRequest, response: ServletResponse = ActionUtil.servletResponse, fileFullName: String = Constants.String.BLANK, saveFilenames: Array<String> = emptyArray()): List<FileUploadResult>? {
        response.contentType = Constants.Http.ContentType.TEXT_PLAIN
        // get content type of client request
        val contentType = request.contentType
        var fileUploadResultList: List<FileUploadResult>? = null
        try {
            if (contentType == null) {
                return fileUploadResultList
            }
            val inputStream = request.inputStream
            val fileUpload = FileUpload()
            val filePath = StaticVar.UPLOAD_FOLDER
            fileUpload.saveFilePath = filePath
            // make sure content type is multipart/form-data,form file use

            if (contentType.indexOf(Constants.Http.ContentType.MULTIPART_FORM_DATA) >= 0) {
                fileUploadResultList = fileUpload.upload(inputStream, request.contentLength, saveFilenames)
            } else if (contentType.indexOf(Constants.Http.ContentType.APPLICATION_OCTET_STREAM) >= 0 || contentType.indexOf(Constants.Http.ContentType.BINARY_OCTET_STREAM) >= 0) {
                if (fileFullName.isNotBlank()) {
                    fileUploadResultList = mutableListOf<FileUploadResult>()
                    val fileUploadResult = fileUpload.upload(inputStream, fileFullName)
                    fileUploadResultList.add(fileUploadResult)
                }
            }// make sure content type is application/octet-stream or binary/octet-stream,flash jpeg picture use or file upload use
        } catch (e: Exception) {
            throw ActionExecuteException(e)
        }

        return fileUploadResultList
    }
}