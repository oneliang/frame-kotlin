package com.oneliang.ktx.frame.tokenization

import com.oneliang.ktx.frame.feature.FeatureOwner

class FeatureOwnerWithDictionary(private val dictionary: Dictionary) : FeatureOwner<String, Dictionary.WordCollector> {

    /**
     * extract feature
     * @param item
     * @return FEATURE
     */
    override fun extractFeature(item: String): Dictionary.WordCollector {
        return this.dictionary.splitWords(item)
    }
}