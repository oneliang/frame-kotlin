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
        var port = 8080
        var hostname = "localhost"
        var baseDir = Constants.String.BLANK
        var webappArray = emptyArray<Webapp>()

        class Webapp {
            var contextPath = Constants.String.BLANK
            var documentBase = Constants.String.BLANK
        }
    }

    private val tomcat = Tomcat()

    fun launch() {
        this.tomcat.setPort(this.configuration.port)
        this.tomcat.setHostname(this.configuration.hostname)
        this.tomcat.setBaseDir(this.configuration.baseDir)
        for (webapp in this.configuration.webappArray) {
            val documentBaseFile = webapp.documentBase.toFile()
            if (!documentBaseFile.exists()) {
                throw FileNotFoundException("document base not found:%s".format(webapp.documentBase))
            }
            val fixDocumentBase = if (documentBaseFile.isFile) {
                var documentBaseFilename = documentBaseFile.name
                documentBaseFilename = documentBaseFilename.substring(0, documentBaseFilename.lastIndexOf(Constants.Symbol.DOT))
                val documentBaseDirectory = File(documentBaseFile.parentFile, documentBaseFilename)
                documentBaseDirectory.createDirectory()
                documentBaseFile.unZip(documentBaseDirectory)
                documentBaseDirectory.absolutePath
            } else {
                webapp.documentBase
            }
            this.tomcat.addWebapp(webapp.contextPath, fixDocumentBase)
        }
        this.tomcat.start()
        this.tomcat.server.await()
    }
}