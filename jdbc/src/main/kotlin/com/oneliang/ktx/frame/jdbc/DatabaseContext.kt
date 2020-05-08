package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourcePool
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

@Deprecated("DatabaseContext")
class DatabaseContext : AbstractContext() {
    companion object {

        private val logger = LoggerManager.getLogger(DatabaseContext::class)
        private val connectionPoolMap = ConcurrentHashMap<String, ResourcePool<Connection>>()
        /**
         *
         * Method: get the connection pool
         * @param poolName
         * @return ResourcePool<Connection>
        </Connection> */
        fun getConnectionPool(poolName: String): ResourcePool<Connection> {
            return connectionPoolMap[poolName]!!
        }

        /**
         * @return the connectionPoolMap
         */
        fun getConnectionPoolMap(): Map<String, ResourcePool<Connection>> {
            return connectionPoolMap
        }
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val properties = FileUtil.getProperties(path)
            properties.forEach { (k, v) ->
                val key = k.toString()
                val value = v.toString()
                val index = key.indexOf(Constants.Symbol.DOT)
                if (index > 0) {
                    val poolName = key.substring(0, index)
                    val propertyName = key.substring(index + 1, key.length)
                    val pool: ResourcePool<Connection> = if (connectionPoolMap.containsKey(poolName)) {
                        connectionPoolMap[poolName]!!
                    } else {
                        val connectionPool = ConnectionPool()
                        connectionPool.resourcePoolName = poolName
                        val dataSource = ConnectionSource()
                        connectionPool.resourceSource = dataSource
                        connectionPoolMap[poolName] = connectionPool
                        connectionPool
                    }
                    when (propertyName) {
                        ConnectionPool.CONNECTION_ALIVE_TIME -> pool.resourceAliveTime = value.toLong()
                        ConnectionPool.THREAD_SLEEP_TIME -> pool.threadSleepTime = value.toLong()
                        ConnectionPool.INITIAL_CONNECTIONS -> pool.minResourceSize = value.toInt()
                        ConnectionPool.MAX_CONNECTIONS -> pool.maxResourceSize = value.toInt()
                        ConnectionSource.CONNECTION_SOURCE_NAME -> (pool.resourceSource as ConnectionSource).connectionSourceName = value
                        ConnectionSource.DRIVER -> (pool.resourceSource as ConnectionSource).driver = value
                        ConnectionSource.URL -> (pool.resourceSource as ConnectionSource).url = value
                        ConnectionSource.USER -> (pool.resourceSource as ConnectionSource).user = value
                        ConnectionSource.PASSWORD -> (pool.resourceSource as ConnectionSource).password = value
                    }
                }
            }
        } catch (e: Throwable) {
            logger.error("parameter:%s", e, fixParameters)
            throw InitializeException(fixParameters, e)
        }
    }

    /**
     * destroy
     */
    override fun destroy() {
        connectionPoolMap.clear()
    }

    /**
     * initial connection pools
     * @throws Exception
     */
    @Throws(Exception::class)
    fun initialConnectionPools() {
        connectionPoolMap.forEach { (_, pool) ->
            try {
                pool.initialize()
            } catch (e: Exception) {
                logger.error(Constants.Base.EXCEPTION, e)
            }
        }
    }
}
