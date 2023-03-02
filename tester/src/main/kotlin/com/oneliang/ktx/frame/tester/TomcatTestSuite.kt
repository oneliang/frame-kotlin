package com.oneliang.ktx.frame.tester

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tomcat.TomcatLauncher
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File

class TomcatTestSuite(private val contextPath: String,
                      private val warFullFilename: String,
                      private val port: Int = 8080) {

    companion object {
        private val logger = LoggerManager.getLogger(TomcatTestSuite::class)
    }

    private val tomcatTestCaseList = mutableListOf<TomcatTestCase>()
    private val configuration = TomcatLauncher.Configuration().also {
        it.port = port
    }
    private lateinit var baseUrl: String

    /**
     * add tomcat test case
     * @param tomcatTestCase
     */
    fun addTomcatTestCase(tomcatTestCase: TomcatTestCase) {
        this.tomcatTestCaseList += tomcatTestCase
    }

    /**
     * run test
     */
    fun runTest() {
        val baseDir = File(Constants.String.BLANK).absolutePath + "/work"
        this.configuration.baseDir = baseDir
        this.configuration.webappArray = arrayOf(TomcatLauncher.Configuration.Webapp().also {
            it.contextPath = contextPath
            it.documentBase = this.warFullFilename
        })
        val tomcatLauncher = TomcatLauncher(this.configuration)
        tomcatLauncher.launch {
            this.baseUrl = getBaseUrl()
            logger.info("Access address: %s", this.baseUrl)
            this.afterLaunch()

        }
    }

    /**
     * get base url
     * @return String
     */
    private fun getBaseUrl(): String {
        return this.configuration.schema + this.configuration.hostname + Constants.Symbol.COLON + this.configuration.port + Constants.Symbol.SLASH_LEFT + contextPath
    }

    /**
     * after launch
     */
    private fun afterLaunch() {
        for (tomcatTestCase in this.tomcatTestCaseList) {
            tomcatTestCase.baseUrl = this.baseUrl
            tomcatTestCase.test()
        }
    }
}