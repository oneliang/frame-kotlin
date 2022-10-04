package com.oneliang.ktx.frame.mongodb

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import com.oneliang.ktx.util.resource.ResourceSource

class MongoClientSource : ResourceSource<MongoClient>() {

    companion object {
        private val logger = LoggerManager.getLogger(MongoClientSource::class)
    }

    var connectionString: String = Constants.String.BLANK

    override val resource: MongoClient
        get() {
            return if (this.connectionString.isBlank()) {
                MongoClients.create()
            } else {
                MongoClients.create(this.connectionString)
            }
        }
}