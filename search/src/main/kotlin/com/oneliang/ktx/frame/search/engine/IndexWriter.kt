package com.oneliang.ktx.frame.search.engine

interface IndexWriter<K, V> {

    /**
     * write index
     * @param key
     * @param value
     */
    fun writeIndex(key: K, value: V)
}