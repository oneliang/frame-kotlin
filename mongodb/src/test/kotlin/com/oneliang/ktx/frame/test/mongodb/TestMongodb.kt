package com.oneliang.ktx.frame.test.mongodb

import com.oneliang.ktx.frame.mongodb.MongoClientPool
import com.oneliang.ktx.frame.mongodb.MongoClientSource
import com.oneliang.ktx.frame.mongodb.MongoQueryImpl
import org.bson.Document

fun createUser(): Document {
    val document = Document()
    document["id"] = 1
    document["id"] = "user"
    return document
}

fun main() {
    val connectionString = "mongodb://localhost:27017"
    val mongoClientSource = MongoClientSource()
    mongoClientSource.connectionString = connectionString
    val mongoClientPool = MongoClientPool()
    mongoClientPool.setResourceSource(mongoClientSource)
    val mongoQuery = MongoQueryImpl()
    mongoQuery.setMongoClientPool(mongoClientPool)
    mongoQuery.useMongoCollection<Unit>("data", "user", {
        val userCount = it.countDocuments()
        println(userCount)
    })
}