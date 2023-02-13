package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.tokenization.FeatureOwner

interface Indexer<ITEM, FEATURE> {

    /**
     * feature owner
     */
    var featureOwner: FeatureOwner<ITEM, FEATURE>

    /**
     * index writer
     */
    var indexWriter: IndexWriter<FEATURE, ITEM>

    /**
     * index
     */
    fun index(item: ITEM, customerFeature: FEATURE? = null)

}