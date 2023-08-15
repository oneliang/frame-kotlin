package com.oneliang.ktx.frame.test.tomcat

import com.oneliang.ktx.frame.tomcat.TomcatLauncher

fun main() {
    val baseDir = "/Users/oneliang/Java/githubWorkspace/oneliang-team/platform/open-platform/api/monitor-reporter/build/libs"
    val tomcatLauncher = TomcatLauncher(TomcatLauncher.Configuration().apply {
        this.baseDir = "/Users/oneliang/Java/githubWorkspace/oneliang-team/platform/open-platform/api/monitor-reporter/build/libs/work"
        this.webAppArray = arrayOf(TomcatLauncher.Configuration.WebApp().apply {
            this.contextPath = "/aaa"
            this.documentBase = "$baseDir/open-platform-api-monitor-reporter-1.0.war"
        })
    })
    tomcatLauncher.launch()
}