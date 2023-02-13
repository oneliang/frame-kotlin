package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.tokenization.Dictionary

class DefaultIndexEngine<ITEM> : IndexEngine<ITEM, Dictionary.WordCollector> {

    override lateinit var indexer: Indexer<ITEM, Dictionary.WordCollector>

    /**
     * index
     * @param value
     */
    override fun index(value: ITEM) {
        if (this::indexer.isInitialized) {
            this.indexer.index(value)
        }
    }
}