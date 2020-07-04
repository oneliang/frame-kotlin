package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourceSource
import java.sql.Connection
import java.sql.DriverManager

/**
 * ConnectionSource bean describe the database detail,include driver,url,user,password
 * four important property
 *
 * @author Dandelion
 * @since 2008-08-22
 */
open class ConnectionSource : ResourceSource<Connection>() {

    companion object {
        private val logger = LoggerManager.getLogger(ConnectionSource::class)
        const val CONNECTION_SOURCE_NAME = "connectionSourceName"
        const val DRIVER = "driver"
        const val URL = "url"
        const val USER = "user"
        const val PASSWORD = "password"
    }

    /**
     * connection source name
     */
    var connectionSourceName: String = Constants.String.BLANK
    /**
     * driver
     */
    var driver: String = Constants.String.BLANK
        set(driver) {
            field = driver
            if (this.driver.isNotBlank()) {
                try {
                    Thread.currentThread().contextClassLoader.loadClass(this.driver).newInstance()
                } catch (e: Exception) {
                    logger.error(Constants.Base.EXCEPTION, e)
                }
            }
        }
    /**
     * url
     */
    var url: String = Constants.String.BLANK
    /**
     * user
     */
    var user: String = Constants.String.BLANK
    /**
     * password
     */
    var password: String = Constants.String.BLANK

    /**
     * Method: initial the connection operate,load the config file or use the
     * default file
     * This method initial the config file
     */
    override val resource: Connection?
        get() {
            return getConnection(this.url, this.user, this.password)
        }

    @Synchronized
    protected fun getConnection(url: String, user: String, password: String): Connection? {
        var connection: Connection? = null
        try {
            connection = DriverManager.getConnection(url, user, password)
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }
        return connection
    }
}
