package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwner
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

class DefaultIndexer : Indexer<DefaultIndexer.ValueItem, Dictionary.WordCollector> {

    companion object {
        private val logger = LoggerManager.getLogger(DefaultIndexer::class)
    }

    override lateinit var featureOwner: FeatureOwner<ValueItem, Dictionary.WordCollector>
    override lateinit var indexWriter: IndexWriter<Dictionary.WordCollector, ValueItem>

    override fun index(item: ValueItem, customerFeature: Dictionary.WordCollector?) {
        //feature is key
        var key = customerFeature
        if (this::featureOwner.isInitialized) {
            key = this.featureOwner.extractFeature(item)
            key.wordList.forEach {
                logger.debug(it.toJson())
            }
        } else {
            logger.error("field:featureOwner has not initialized")
        }
        if (this::indexWriter.isInitialized) {
            if (key != null) {
                this.indexWriter.write(key, item)
            }
        } else {
            logger.warning("field:indexWriter has not initialized")
        }
    }

    class ValueItem(val value: String, val directory: String = Constants.String.BLANK)
}