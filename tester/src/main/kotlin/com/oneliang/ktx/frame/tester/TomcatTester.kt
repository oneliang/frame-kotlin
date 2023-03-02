package com.oneliang.ktx.frame.tester

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tomcat.TomcatLauncher
import java.io.File

open class TomcatTester(private val warFullFilename: String) {

    /**
     * before test
     */
    private fun beforeTest() {
        val baseDir = File(Constants.String.BLANK).absolutePath + "/work"
        val contextPath = File(this.warFullFilename).name
        println(baseDir)

        val configuration = TomcatLauncher.Configuration()
        configuration.baseDir = baseDir
        configuration.webappArray = arrayOf(TomcatLauncher.Configuration.Webapp().also {
            it.contextPath = contextPath
            it.documentBase = this.warFullFilename
        })
        val tomcatLauncher = TomcatLauncher(configuration)
        tomcatLauncher.launch(this::test)
    }

    private fun afterTest() {
        println("afterTest")
    }

    /**
     * test
     */
    open fun test() {
        println("test")
    }

    fun runTest() {
        beforeTest()
        afterTest()
    }
}