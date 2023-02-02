package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwner
import com.oneliang.ktx.util.logging.LoggerManager

class DefaultIndexer : Indexer<String, Dictionary.WordCollector> {

    companion object {
        private val logger = LoggerManager.getLogger(DefaultIndexer::class)
    }

    override lateinit var featureOwner: FeatureOwner<String, Dictionary.WordCollector>
    override lateinit var indexWriter: IndexWriter<Dictionary.WordCollector, String>

    override fun index(value: String, customerFeature: Dictionary.WordCollector?) {
        var key = customerFeature
        if (this::featureOwner.isInitialized) {
            key = this.featureOwner.extractFeature(value)
        } else {
            logger.error("field:featureOwner has not initialized")
        }
        if (this::indexWriter.isInitialized) {
            if (key != null) {
                this.indexWriter.writeIndex(key, value)
            }
        } else {
            logger.error("field:indexWriter has not initialized")
        }
    }
}