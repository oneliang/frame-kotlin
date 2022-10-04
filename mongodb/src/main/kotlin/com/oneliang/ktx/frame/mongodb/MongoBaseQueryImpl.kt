package com.oneliang.ktx.frame.mongodb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.oneliang.ktx.util.logging.LoggerManager

open class MongoBaseQueryImpl : MongoBaseQuery {
    companion object {
        private val logger = LoggerManager.getLogger(MongoBaseQueryImpl::class)
    }

    /**
     * use mongo client
     * @param mongoClient
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    override fun <R> useMongoClient(mongoClient: MongoClient, block: (mongoClient: MongoClient) -> R): R {
        return block(mongoClient)
    }

    /**
     * use mongo database
     * @param mongoClient
     * @param databaseName
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    override fun <R> useMongoDatabase(mongoClient: MongoClient, databaseName: String, block: (mongoDatabase: MongoDatabase) -> R): R {
        val mongoDatabase = mongoClient.getDatabase(databaseName)
        return block(mongoDatabase)
    }

    /**
     * use mongo collection
     * @param mongoClient
     * @param databaseName
     * @param collectionName
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    @Suppress("UNCHECKED_CAST")
    override fun <R> useMongoCollection(mongoClient: MongoClient, databaseName: String, collectionName: String, block: (mongoCollection: MongoCollection<R>) -> R): R {
        val mongoDatabase = mongoClient.getDatabase(databaseName)
        val mongoCollection = mongoDatabase.getCollection(collectionName) as MongoCollection<R>
        return block(mongoCollection)
    }
}