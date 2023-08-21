package com.oneliang.ktx.frame.tester

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tomcat.TomcatLauncher
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File

class TomcatTestSuite(
    private val webAppArray: Array<TomcatLauncher.Configuration.WebApp>,
    private val port: Int = 8080
) {

    companion object {
        private val logger = LoggerManager.getLogger(TomcatTestSuite::class)
    }

    private val httpTestCaseList = mutableListOf<HttpTestCase>()
    private val configuration = TomcatLauncher.Configuration().also {
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
        val baseDir = File(Constants.String.BLANK).absolutePath + "/work"
        this.configuration.baseDir = baseDir
        this.configuration.webAppArray = this.webAppArray
        val tomcatLauncher = TomcatLauncher(this.configuration)
        tomcatLauncher.launch {
            val baseUrlMap = mutableMapOf<String, String>()
            for (webApp in this.webAppArray) {
                val baseUrl = getBaseUrl(webApp.contextPath)
                baseUrlMap[webApp.contextPath] = baseUrl
                logger.info("Access context:%s, address: %s", webApp.contextPath, baseUrl)
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
        val trimContextPath = contextPath.trim()
        val fixContextPath = if (trimContextPath.startsWith(Constants.Symbol.SLASH_LEFT)) {
            trimContextPath
        } else {
            Constants.Symbol.SLASH_LEFT + trimContextPath
        }
        return this.configuration.schema + this.configuration.hostname + Constants.Symbol.COLON + this.configuration.port + fixContextPath
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