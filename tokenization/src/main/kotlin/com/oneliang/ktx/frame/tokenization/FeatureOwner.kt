package com.oneliang.ktx.frame.tokenization

interface FeatureOwner<ITEM, FEATURE> {

    /**
     * extract feature
     * @param item
     * @return FEATURE
     */
    fun extractFeature(item: ITEM): FEATURE
}