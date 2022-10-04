package com.oneliang.ktx.frame.mongodb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.oneliang.ktx.util.resource.ResourcePool

class MongoQueryImpl : MongoBaseQueryImpl(), MongoQuery {

    private lateinit var mongoClientPool: ResourcePool<MongoClient>

    /**
     * use mongo client
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    override fun <R> useMongoClient(block: (mongoClient: MongoClient) -> R): R {
        var mongoClient: MongoClient? = null
        return try {
            mongoClient = this.mongoClientPool.resource
            useMongoClient(mongoClient, block)
        } catch (e: Throwable) {
            throw MongoQueryException(e)
        } finally {
            this.mongoClientPool.releaseResource(mongoClient)
        }
    }

    /**
     * use stable mongo client
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    override fun <R> useStableMongoClient(block: (mongoClient: MongoClient) -> R): R {
        var mongoClient: MongoClient? = null
        return try {
            mongoClient = this.mongoClientPool.stableResource
            useMongoClient(mongoClient, block)
        } catch (e: Throwable) {
            throw MongoQueryException(e)
        } finally {
            this.mongoClientPool.releaseStableResource(mongoClient)
        }
    }

    /**
     * use suitable mongo client
     * @param useStable
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    private fun <R> useSuitableMongoClient(useStable: Boolean, block: (mongoClient: MongoClient) -> R): R {
        return if (useStable) {
            useStableMongoClient(block)
        } else {
            useMongoClient(block)
        }
    }

    /**
     * use mongo database
     * @param databaseName
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    override fun <R> useMongoDatabase(databaseName: String, block: (mongoDatabase: MongoDatabase) -> R, useStable: Boolean): R {
        return useSuitableMongoClient(useStable) {
            useMongoDatabase(it, databaseName, block)
        }
    }

    /**
     * use mongo collection
     * @param databaseName
     * @param collectionName
     * @param block
     * @param useStable
     * @return R
     * @throws MongoQueryException
     */
    @Suppress("UNCHECKED_CAST")
    override fun <R> useMongoCollection(databaseName: String, collectionName: String, block: (mongoCollection: MongoCollection<R>) -> R, useStable: Boolean): R {
        return useSuitableMongoClient(useStable) {
            useMongoCollection(it, databaseName, collectionName, block)
        }
    }

    fun setMongoClientPool(mongoClientPool: ResourcePool<MongoClient>) {
        this.mongoClientPool = mongoClientPool
    }
}