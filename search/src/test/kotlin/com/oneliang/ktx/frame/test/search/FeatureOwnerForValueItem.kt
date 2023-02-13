package com.oneliang.ktx.frame.test.search

import com.oneliang.ktx.frame.search.engine.DefaultIndexer
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwner
import com.oneliang.ktx.frame.tokenization.FeatureOwnerWithDictionary

class FeatureOwnerForValueItem(dictionary: Dictionary) : FeatureOwner<DefaultIndexer.ValueItem, Dictionary.WordCollector> {

    private val featureOwnerWithDictionary = FeatureOwnerWithDictionary(dictionary)

    /**
     * extract feature
     * @param item
     * @return Dictionary.WordCollector
     */
    override fun extractFeature(item: DefaultIndexer.ValueItem): Dictionary.WordCollector {
        return this.featureOwnerWithDictionary.extractFeature(item.value)
    }
}