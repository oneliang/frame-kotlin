package com.oneliang.ktx.frame.search.engine

interface ResourceReader<K, V> {

    /**
     * read key from index
     * @param key
     * @return List<V>
     */
    fun read(key: K): List<V>
}