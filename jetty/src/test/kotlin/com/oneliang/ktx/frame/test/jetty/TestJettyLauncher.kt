package com.oneliang.ktx.frame.test.jetty

import com.oneliang.ktx.frame.jetty.JettyLauncher

fun main() {
    val testJettyLauncher = JettyLauncher(JettyLauncher.Configuration().also {
        it.port = 8080
        it.webApp = JettyLauncher.Configuration.WebApp().also { webApp ->
            webApp.contextPath = "/"
            webApp.war = "/Users/oneliang/Java/githubWorkspace/oneliang-team/me/me-platform/internal-platform/api/internal-privilege/build/libs/internal-platform-api-internal-privilege-1.0.war"
        }
    })

    testJettyLauncher.launch { }
}