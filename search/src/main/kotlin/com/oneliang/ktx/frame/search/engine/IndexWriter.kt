package com.oneliang.ktx.frame.search.engine

interface IndexWriter<K, V> {

    /**
     * write
     * @param key
     * @param value
     */
    fun write(key: K, value: V)
}