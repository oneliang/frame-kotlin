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

        //config file
        if (configFile.isNotBlank()) {
            try {
                val configurationContext = ConfigurationContainer.rootConfigurationContext
                var classesRealPath = Thread.currentThread().contextClassLoader.getResource(Constants.String.BLANK)?.path.nullToBlank()
                classesRealPath = File(classesRealPath).absolutePath
                configurationContext.classesRealPath = classesRealPath
                projectRealPath = File(projectRealPath).absolutePath
                configurationContext.projectRealPath = projectRealPath
                configurationContext.initialize(configFile)
                afterConfigurationInitialize(configurationContext)
            } catch (e: Throwable) {
                e.printStackTrace()
                logger.error(Constants.Base.EXCEPTION, e)
            }
        } else {
            logger.error("config file is not found,please initial the config file")
        }
    }

    /**
     * when the server is shut down,close the connection pool
     */
    override fun contextDestroyed(servletContextEvent: ServletContextEvent) {
        val configurationContext = ConfigurationContainer.rootConfigurationContext
        configurationContext.destroyAll()
    }

    @Throws(Exception::class)
    abstract fun afterConfigurationInitialize(configurationContext: ConfigurationContext)
}
