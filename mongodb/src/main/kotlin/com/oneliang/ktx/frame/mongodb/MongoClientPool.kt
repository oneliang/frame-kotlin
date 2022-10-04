package com.oneliang.ktx.frame.mongodb

import com.mongodb.client.MongoClient
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourcePool
import com.oneliang.ktx.util.resource.ResourcePoolException

class MongoClientPool : ResourcePool<MongoClient>() {

    companion object {
        private val logger = LoggerManager.getLogger(MongoClientPool::class)
    }

    override fun destroyResource(resource: MongoClient?) {
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
}