package com.oneliang.ktx.frame.mongodb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase

interface MongoQuery : MongoBaseQuery {

    /**
     * use mongo client
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    @Throws(MongoQueryException::class)
    fun <R> useMongoClient(block: (mongoClient: MongoClient) -> R): R

    /**
     * use stable mongo client
     * @param block
     * @return R
     * @throws MongoQueryException
     */
    @Throws(MongoQueryException::class)
    fun <R> useStableMongoClient(block: (mongoClient: MongoClient) -> R): R

    /**
     * use mongo database
     * @param databaseName
     * @param block
     * @param useStable
     * @return R
     * @throws MongoQueryException
     */
    @Throws(MongoQueryException::class)
    fun <R> useMongoDatabase(databaseName: String, block: (mongoDatabase: MongoDatabase) -> R, useStable: Boolean = false): R

    /**
     * use mongo collection
     * @param databaseName
     * @param collectionName
     * @param block
     * @param useStable
     * @return R
     * @throws MongoQueryException
     */
    @Throws(MongoQueryException::class)
    fun <R> useMongoCollection(databaseName: String, collectionName: String, block: (mongoCollection: MongoCollection<R>) -> R, useStable: Boolean = false): R
}