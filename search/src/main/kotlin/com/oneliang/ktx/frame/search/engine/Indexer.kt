package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.tokenization.FeatureOwner

interface Indexer<ITEM, FEATURE> {

    var featureOwner: FeatureOwner<ITEM, FEATURE>
    var indexWriter: IndexWriter<FEATURE, ITEM>

    fun index(value: String, customerFeature: FEATURE? = null)

}