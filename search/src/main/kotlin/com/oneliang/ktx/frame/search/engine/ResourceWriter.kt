package com.oneliang.ktx.frame.search.engine

interface ResourceWriter<K, V> {

    /**
     * write
     * @param key
     * @param value
     */
    fun write(key: K, value: V)
}