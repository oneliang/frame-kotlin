package com.oneliang.ktx.frame.jetty

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.webapp.WebAppContext

class JettyLauncher(private val configuration: Configuration) {
    companion object {
        private val logger = LoggerManager.getLogger(JettyLauncher::class)
    }

    class Configuration {
        var schema = Constants.Protocol.HTTP
        var port = 8080
        var hostname = "localhost"
        var webApp: WebApp? = null

        class WebApp(var contextPath: String = Constants.String.BLANK, var war: String = Constants.String.BLANK)
    }

    private val server = Server()
    private val connector = ServerConnector(this.server)

    init {
        this.server.addConnector(this.connector)
    }

    fun launch(afterStart: () -> Unit = {}) {
        this.connector.port = this.configuration.port
        val webApp = this.configuration.webApp
        if (webApp == null) {
            logger.warning("webApp is null, please set it.")
        } else {
            val webAppContext = WebAppContext()
            webAppContext.contextPath = webApp.contextPath
            webAppContext.war = webApp.war
            this.server.handler = webAppContext
        }
        this.server.start()
        afterStart()
        this.server.join()
    }
}