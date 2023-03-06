package com.oneliang.ktx.frame.tester

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tomcat.TomcatLauncher
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File

class TomcatTestSuite(private val webappArray: Array<TomcatLauncher.Configuration.Webapp>,
                      private val port: Int = 8080) {

    companion object {
        private val logger = LoggerManager.getLogger(TomcatTestSuite::class)
    }

    private val tomcatTestCaseList = mutableListOf<TomcatTestCase>()
    private val configuration = TomcatLauncher.Configuration().also {
        it.port = port
    }

    private lateinit var baseUrlMap: Map<String, String>

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
        this.configuration.webappArray = this.webappArray
        val tomcatLauncher = TomcatLauncher(this.configuration)
        tomcatLauncher.launch {
            val baseUrlMap = mutableMapOf<String, String>()
            for (webapp in this.webappArray) {
                val baseUrl = getBaseUrl(webapp.contextPath)
                baseUrlMap[webapp.contextPath] = baseUrl
                logger.info("Access context:%s, address: %s", webapp.contextPath, baseUrl)
            }
            this.baseUrlMap = baseUrlMap
            this.afterLaunch()

        }
    }

    /**
     * get base url
     * @return String
     */
    private fun getBaseUrl(contextPath: String): String {
        return this.configuration.schema + this.configuration.hostname + Constants.Symbol.COLON + this.configuration.port + Constants.Symbol.SLASH_LEFT + contextPath
    }

    /**
     * after launch
     */
    private fun afterLaunch() {
        for (tomcatTestCase in this.tomcatTestCaseList) {
            tomcatTestCase.baseUrlMap = this.baseUrlMap
            tomcatTestCase.test()
        }
    }
}