package com.oneliang.ktx.frame.tomcat

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.file.createDirectory
import com.oneliang.ktx.util.file.unZip
import org.apache.catalina.startup.Tomcat
import java.io.File
import java.io.FileNotFoundException

class TomcatLauncher(private val configuration: Configuration) {
    class Configuration {
        var schema = Constants.Protocol.HTTP
        var port = 8080
        var hostname = "localhost"
        var baseDir = Constants.String.BLANK
        var webAppArray = emptyArray<WebApp>()

        class WebApp(var contextPath: String = Constants.String.BLANK, var documentBase: String = Constants.String.BLANK)
    }

    private val tomcat = Tomcat()

    fun launch(afterStart: () -> Unit = {}) {
        this.tomcat.setPort(this.configuration.port)
        this.tomcat.setHostname(this.configuration.hostname)
        this.tomcat.setBaseDir(this.configuration.baseDir)
        for (webApp in this.configuration.webAppArray) {
            val documentBaseFile = webApp.documentBase.toFile()
            if (!documentBaseFile.exists()) {
                throw FileNotFoundException("document base not found:%s".format(webApp.documentBase))
            }
            val fixDocumentBase = if (documentBaseFile.isFile) {
                var documentBaseFilename = documentBaseFile.name
                documentBaseFilename = documentBaseFilename.substring(0, documentBaseFilename.lastIndexOf(Constants.Symbol.DOT))
                val documentBaseDirectory = File(documentBaseFile.parentFile, documentBaseFilename)
                if (documentBaseDirectory.exists()) {
                    documentBaseDirectory.deleteRecursively()
                }
                documentBaseDirectory.createDirectory()
                documentBaseFile.unZip(documentBaseDirectory)
                documentBaseDirectory.absolutePath
            } else {
                webApp.documentBase
            }
            this.tomcat.addWebapp(webApp.contextPath, fixDocumentBase)
        }
        this.tomcat.connector
        this.tomcat.start()
        afterStart()
        this.tomcat.server.await()
    }
}