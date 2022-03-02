package com.oneliang.ktx.frame.test.tomcat

import com.oneliang.ktx.frame.tomcat.TomcatLauncher

fun main() {
    val tomcatLauncher = TomcatLauncher(TomcatLauncher.Configuration().apply {
        this.baseDir = "/D:/temp/work"
        this.webappArray = arrayOf(TomcatLauncher.Configuration.Webapp().apply {
            this.contextPath = "/price"
            this.documentBase = "/D:/temp/price.war"
        })
    })
    tomcatLauncher.launch()
}