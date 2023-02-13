package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.tokenization.FeatureOwner

interface Searcher<ITEM, FEATURE, VALUE> {

    var featureOwner: FeatureOwner<ITEM, FEATURE>
    var indexReader: IndexReader<ITEM, VALUE>

    fun search(value: ITEM, customerFeature: FEATURE? = null): List<VALUE>
}