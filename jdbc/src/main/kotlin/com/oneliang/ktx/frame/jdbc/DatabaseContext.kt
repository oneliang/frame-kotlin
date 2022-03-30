package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AbstractContext
import com.oneliang.ktx.util.file.toProperties
import com.oneliang.ktx.util.logging.LoggerManager

class DatabaseContext : AbstractContext() {
    companion object {
        private val logger = LoggerManager.getLogger(DatabaseContext::class)
    }

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        val fixParameters = fixParameters(parameters)
        try {
            val path = this.classesRealPath + fixParameters
            val properties = path.toProperties()
            val connectionPoolMap = mutableMapOf<String, ConnectionPool>()
            val resourceSourceMap = mutableMapOf<String, ConnectionSource>()
            properties.forEach { (k, v) ->
                val key = k.toString()
                val value = v.toString()
                val index = key.indexOf(Constants.Symbol.DOT)
                if (index >= 0) {
                    val id = key.substring(0, index)
                    val propertyName = key.substring(index + 1, key.length)
                    val connectionPool = connectionPoolMap.getOrPut(id) {
                        ConnectionPool().also {
                            it.resourcePoolName = id
                        }
                    }
                    val connectionSource = resourceSourceMap.getOrPut(id) {
                        ConnectionSource().also {
                            connectionPool.setResourceSource(it)
                        }
                    }
                    when (propertyName) {
                        ConnectionSource.CONNECTION_SOURCE_NAME -> connectionSource.connectionSourceName = value
                        ConnectionSource.DRIVER -> connectionSource.driver = value
                        ConnectionSource.URL -> connectionSource.url = value
                        ConnectionSource.USER -> connectionSource.user = value
                        ConnectionSource.PASSWORD -> connectionSource.password = value
                        ConnectionPool.CONNECTION_ALIVE_TIME -> connectionPool.resourceAliveTime = value.toLong()
                        ConnectionPool.MIN_CONNECTIONS -> connectionPool.minResourceSize = value.toInt()
                        ConnectionPool.MAX_CONNECTIONS -> connectionPool.maxResourceSize = value.toInt()
                        ConnectionPool.THREAD_SLEEP_TIME -> connectionPool.threadSleepTime = value.toLong()
                        ConnectionPool.MAX_STABLE_RESOURCE_SIZE -> connectionPool.maxStableResourceSize = value.toInt()
                    }
                }
            }
            logger.info("connection pool size:%s".format(connectionPoolMap.size))
            connectionPoolMap.forEach { (id, connectionPool) ->
                val query: Query = DefaultQueryImpl().apply {
                    this.setConnectionPool(connectionPool)
                    this.setSqlProcessor(DefaultSqlProcessor())
                }
                connectionPool.initialize()
                if (id.isBlank()) {
                    objectMap["query"] = ObjectBean(query, ObjectBean.Type.REFERENCE_BOTH)
                } else {
                    objectMap[id + "Query"] = ObjectBean(query, ObjectBean.Type.REFERENCE_BOTH)
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
    override fun destroy() {}
}
