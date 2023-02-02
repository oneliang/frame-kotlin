package com.oneliang.ktx.frame.tokenization

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