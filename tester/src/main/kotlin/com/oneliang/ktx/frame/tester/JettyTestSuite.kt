package com.oneliang.ktx.frame.tester

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.jetty.JettyLauncher
import com.oneliang.ktx.util.logging.LoggerManager

class JettyTestSuite(
    private val webApp: JettyLauncher.Configuration.WebApp,
    private val port: Int = 8080
) {

    companion object {
        private val logger = LoggerManager.getLogger(JettyTestSuite::class)
    }

    private val httpTestCaseList = mutableListOf<HttpTestCase>()
    private val configuration = JettyLauncher.Configuration().also {
        it.port = port
    }

    private lateinit var baseUrlMap: Map<String, String>

    /**
     * add http test case
     * @param httpTestCase
     */
    fun addHttpTestCase(httpTestCase: HttpTestCase) {
        this.httpTestCaseList += httpTestCase
    }

    /**
     * run test
     */
    fun runTest() {
        val tomcatLauncher = JettyLauncher(this.configuration)
        tomcatLauncher.launch {
            val baseUrlMap = mutableMapOf<String, String>()
            val baseUrl = getBaseUrl(this.webApp.contextPath)
            baseUrlMap[this.webApp.contextPath] = baseUrl
            logger.info("Access context:%s, address: %s", this.webApp.contextPath, baseUrl)
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
        for (tomcatTestCase in this.httpTestCaseList) {
            tomcatTestCase.baseUrlMap = this.baseUrlMap
            tomcatTestCase.test()
        }
    }
}