package com.oneliang.ktx.frame.search.engine

interface IndexEngine<ITEM, FEATURE> {

    var indexer: Indexer<ITEM, FEATURE>

    fun index(value: ITEM)
}