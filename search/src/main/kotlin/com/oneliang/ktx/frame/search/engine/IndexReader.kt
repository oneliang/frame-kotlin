package com.oneliang.ktx.frame.search.engine

interface IndexReader<K, V> {

    /**
     * read key from index
     * @param key
     * @return List<V>
     */
    fun read(key: K): List<V>
}