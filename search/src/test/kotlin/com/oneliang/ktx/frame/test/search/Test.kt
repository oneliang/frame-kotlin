package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.search.FileStorage
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwnerWithDictionary

private fun loadDictionary(): Dictionary {
    val dictionaryFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/main/resources/main2012.dic"
    val dictionary = Dictionary()
    dictionary.load(dictionaryFullFilename)
    return dictionary
}

fun main() {
    val dictionary = loadDictionary()
    val indexDirectory = "/Users/oneliang/Java/temp"
    val fileStorage = FileStorage(indexDirectory)
    val indexEngine = DefaultIndexEngine()
    val indexWriter = DefaultIndexWriter(fileStorage).also { it.initialize() }
    indexEngine.indexer = DefaultIndexer().also {
        it.indexWriter = indexWriter
        it.featureOwner = FeatureOwnerWithDictionary(dictionary)
    }
    val content = "求购201宏旺J1 0.5卷一个"
    indexEngine.index(content)
}