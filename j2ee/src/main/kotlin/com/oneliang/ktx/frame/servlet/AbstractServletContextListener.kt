package com.oneliang.ktx.frame.servlet

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.configuration.ConfigurationContainer
import com.oneliang.ktx.frame.configuration.ConfigurationContext
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.replaceAllLines
import com.oneliang.ktx.util.common.replaceAllSpace
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.*
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

abstract class AbstractServletContextListener : ServletContextListener {
    companion object {
        private val logger = LoggerManager.getLogger(AbstractServletContextListener::class)

        private const val CONTEXT_PARAMETER_CONFIG_FILE = "configFile"
    }

    /**
     * when the server is starting initial all thing
     */
    override fun contextInitialized(servletContextEvent: ServletContextEvent) {
        TimeZone.setDefault(TimeZone.getTimeZone(Constants.TimeZone.ASIA_SHANGHAI))
        Locale.setDefault(Locale.CHINA)
        val configFile = servletContextEvent.servletContext.getInitParameter(CONTEXT_PARAMETER_CONFIG_FILE).nullToBlank().replaceAllSpace().replaceAllLines()
        //real path
        var projectRealPath = servletContextEvent.servletContext.getRealPath(Constants.String.BLANK).nullToBlank()

        try {
            projectRealPath = File(projectRealPath).absolutePath
            beforeConfigurationContextInitialize(projectRealPath)//always initialize logger here


            val configurationContext = ConfigurationContainer.rootConfigurationContext
            configurationContext.projectRealPath = projectRealPath
            //parameter:configFile can be blank, it is supported by configuration context
            configurationContext.initialize(configFile)
            if (configFile.isBlank()) {
                logger.info("config file is blank, maybe use dsl config")
            }
            afterConfigurationContextInitialize(configurationContext)
        } catch (e: Throwable) {
            e.printStackTrace()
            logger.error(Constants.String.EXCEPTION, e)
        }
    }

    /**
     * when the server is shut down,close the connection pool
     */
    override fun contextDestroyed(servletContextEvent: ServletContextEvent) {
        val configurationContext = ConfigurationContainer.rootConfigurationContext
        configurationContext.destroyAll()
    }

    /**
     * always use to initialize logger here, before configuration context initialize
     * @param projectRealPath
     */
    @Throws(Exception::class)
    protected open fun beforeConfigurationContextInitialize(projectRealPath: String) {
    }

    /**
     * after configuration context initialize
     * @param configurationContext
     */
    @Throws(Exception::class)
    protected abstract fun afterConfigurationContextInitialize(configurationContext: ConfigurationContext)
}
