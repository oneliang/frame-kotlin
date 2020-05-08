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
class ConnectionSource : ResourceSource<Connection>() {

    companion object {
        private val logger = LoggerManager.getLogger(ConnectionSource::class)
        const val CONNECTION_SOURCE_NAME = "connectionSourceName"
        const val DRIVER = "driver"
        const val URL = "url"
        const val USER = "user"
        const val PASSWORD = "password"
    }
    /**
     * data base properties
     */
    /**
     * @return the connectionSourceName
     */
    /**
     * @param connectionSourceName the connectionSourceName to set
     */
    var connectionSourceName: String = Constants.String.BLANK
    /**
     * @return the driver
     */
    /**
     * @param driver
     * the driver to set
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
     * @return the url
     */
    /**
     * @param url
     * the url to set
     */
    var url: String = Constants.String.BLANK
    /**
     * @return the user
     */
    /**
     * @param user
     * the user to set
     */
    var user: String = Constants.String.BLANK
    /**
     * @return the password
     */
    /**
     * @param password
     * the password to set
     */
    var password: String = Constants.String.BLANK

    /**
     * Method: initial the connection operate,load the config file or use the
     * default file
     * This method initial the config file
     */
    override val resource: Connection?
        @Synchronized get() {
            var connection: Connection? = null
            try {
                connection = DriverManager.getConnection(this.url, this.user, this.password)
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }
            return connection
        }
}
