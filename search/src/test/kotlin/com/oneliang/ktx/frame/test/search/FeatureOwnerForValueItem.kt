package com.oneliang.ktx.frame.test.search

import com.oneliang.ktx.frame.search.engine.DefaultDataEngine
import com.oneliang.ktx.frame.feature.FeatureOwner
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwnerWithDictionary

class FeatureOwnerForValueItem(dictionary: Dictionary) : FeatureOwner<DefaultDataEngine.ValueItem, Dictionary.WordCollector> {

    private val featureOwnerWithDictionary = FeatureOwnerWithDictionary(dictionary)

    /**
     * extract feature
     * @param item
     * @return Dictionary.WordCollector
     */
    override fun extractFeature(item: DefaultDataEngine.ValueItem): Dictionary.WordCollector {
        return this.featureOwnerWithDictionary.extractFeature(item.value)
    }
}