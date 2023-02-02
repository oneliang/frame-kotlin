package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.tokenization.Dictionary

class DefaultIndexEngine : IndexEngine<String, Dictionary.WordCollector> {

    override lateinit var indexer: Indexer<String, Dictionary.WordCollector>

    /**
     * index
     * @param value
     */
    override fun index(value: String) {
        if (this::indexer.isInitialized) {
            this.indexer.index(value)
        }
    }
}