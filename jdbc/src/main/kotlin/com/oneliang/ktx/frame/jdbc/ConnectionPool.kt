package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.util.resource.ResourcePool
import com.oneliang.ktx.util.resource.ResourcePoolException
import java.sql.Connection

/**
 * class ConnectionPool,manager one dataSource connection
 *
 * @author Dandelion
 * @since 2008-08-25
 */
class ConnectionPool : ResourcePool<Connection>() {
    companion object {
        const val INITIAL_CONNECTIONS = "initialConnections"
        const val MAX_CONNECTIONS = "maxConnections"
        const val CONNECTION_ALIVE_TIME = "connectionAliveTime"
        const val THREAD_SLEEP_TIME = "threadSleepTime"
    }

    private var connectionPoolProcessor: ConnectionPoolProcessor? = null

    override val resource: Connection?
        @Throws(ResourcePoolException::class)
        get() {
            val connection: Connection?
            val customTransaction = TransactionManager.isCustomTransaction()
            if (customTransaction) {
                if (TransactionManager.customTransactionConnection.get() != null) {
                    connection = TransactionManager.customTransactionConnection.get()
                } else {
                    connection = super.resource
                    TransactionManager.customTransactionConnection.set(connection)
                }
            } else {
                connection = super.resource
            }
            return connection
        }

    override fun initialize() {
        super.initialize()
        this.connectionPoolProcessor?.afterInitialize()
    }

    override fun releaseResource(resource: Connection?, destroy: Boolean) {
        val customTransaction = TransactionManager.isCustomTransaction()
        if (!customTransaction) {
            super.releaseResource(resource, destroy)
            if (TransactionManager.customTransactionConnection.get() != null) {
                TransactionManager.customTransactionConnection.set(null)
            }
        }
    }

    /**
     * close the connection
     */
    @Throws(ResourcePoolException::class)
    override fun destroyResource(resource: Connection?) {
        if (resource != null) {
            try {
                resource.close()
            } catch (e: Exception) {
                throw ResourcePoolException(e)
            } finally {
                try {
                    resource.close()
                } catch (e: Exception) {
                    throw ResourcePoolException(e)
                }
            }
        }
    }

    /**
     * @param connectionPoolProcessor the connectionPoolProcessor to set
     */
    fun setConnectionPoolProcessor(connectionPoolProcessor: ConnectionPoolProcessor) {
        this.connectionPoolProcessor = connectionPoolProcessor
    }

    interface ConnectionPoolProcessor {

        /**
         * after initialize
         */
        fun afterInitialize()
    }
}
