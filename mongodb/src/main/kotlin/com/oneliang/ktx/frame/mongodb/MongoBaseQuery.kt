package com.oneliang.ktx.frame.mongodb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase

/**
 * MongoBaseQuery interface base on the MongoClient.
 * @author Dandelion
 * @since 2022-10-02
 */
interface MongoBaseQuery {

    /**
     * use mongo client
     * @param mongoClient
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    @Throws(MongoQueryException::class)
    fun <R> useMongoClient(mongoClient: MongoClient, block: (mongoClient: MongoClient) -> R): R

    /**
     * use mongo database
     * @param mongoClient
     * @param databaseName
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    @Throws(MongoQueryException::class)
    fun <R> useMongoDatabase(mongoClient: MongoClient, databaseName: String, block: (mongoDatabase: MongoDatabase) -> R): R

    /**
     * use mongo collection
     * @param mongoClient
     * @param databaseName
     * @param collectionName
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    @Throws(MongoQueryException::class)
    fun <R> useMongoCollection(mongoClient: MongoClient, databaseName: String, collectionName: String, block: (mongoCollection: MongoCollection<R>) -> R): R
}
