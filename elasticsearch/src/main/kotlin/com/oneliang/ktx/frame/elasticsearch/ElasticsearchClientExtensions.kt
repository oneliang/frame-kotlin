package com.oneliang.ktx.frame.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.GetRequest
import co.elastic.clients.elasticsearch.core.GetResponse
import co.elastic.clients.elasticsearch.core.IndexResponse
import kotlin.reflect.KClass

fun <T : Any> ElasticsearchClient.index(indexValue: String, idValue: String?, document: T): IndexResponse {
    return this.index<T> { i ->
        i.index(indexValue).id(idValue).document(document)
    }
}

inline fun <T : Any> ElasticsearchClient.get(indexValue: String, idValue: String, kClass: KClass<T>, whenFound: (GetResponse<T>, T) -> Unit = { _, _ -> }, whenNotFound: (GetResponse<T>) -> Unit = {}) {
    val getResponse = this.get(
        { g: GetRequest.Builder ->
            g.index(indexValue).id(idValue)
        },
        kClass.java
    )

    if (getResponse.found()) {
        val source = getResponse.source()
        if (source != null) {
            whenFound(getResponse, source)
        } else {
            error("getResponse.source() is null")
        }
    } else {
        whenNotFound(getResponse)
    }
}