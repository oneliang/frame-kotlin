package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwner
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

class DefaultDataEngine(private val featureOwner: FeatureOwner<ValueItem, Dictionary.WordCollector>,
                        private val resourceReader: ResourceReader<String, String>,
                        private val resourceWriter: ResourceWriter<Dictionary.WordCollector, ValueItem>) : DataEngine<DefaultDataEngine.ValueItem> {

    companion object {
        private val logger = LoggerManager.getLogger(DefaultDataEngine::class)
    }

    override fun index(item: ValueItem) {
        //feature is key
        val key = this.featureOwner.extractFeature(item)
        key.wordList.forEach {
            logger.debug(it.toJson())
        }
        this.resourceWriter.write(key, item)
    }

    override fun search(key: String): List<String> {
        return this.resourceReader.read(key)
    }

    class ValueItem(val value: String, val directory: String = Constants.String.BLANK)
}