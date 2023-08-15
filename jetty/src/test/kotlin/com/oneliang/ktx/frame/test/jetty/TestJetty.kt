package com.oneliang.ktx.frame.test.jetty

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.webapp.WebAppContext


@Throws(Exception::class)
fun main(args: Array<String>) {
    val server = Server()
    val connector = ServerConnector(server)
    connector.setPort(8080)
    server.addConnector(connector)
    val webApp = WebAppContext()
    webApp.contextPath = "/"
    webApp.war = "/Users/oneliang/Java/githubWorkspace/oneliang-team/me/me-platform/internal-platform/api/internal-privilege/build/libs/internal-platform-api-internal-privilege-1.0.war"
    server.setHandler(webApp)
    server.start()
    server.join()
}