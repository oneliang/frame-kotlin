package com.oneliang.ktx.frame.search.engine

interface DataEngine<ITEM> {

    /**
     * index
     * @param item
     */
    fun index(item: ITEM)

    /**
     * search
     * @param key
     * @return List<String>
     */
    fun search(key: String): List<String>
}