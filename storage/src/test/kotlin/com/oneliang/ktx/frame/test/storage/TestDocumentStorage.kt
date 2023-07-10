package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.DocumentStorage
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwnerWithDictionary

private fun loadDictionary(): Dictionary {
    val dictionaryFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/main/resources/main.dic"
    val dictionary = Dictionary()
    dictionary.load(dictionaryFullFilename)
    return dictionary
}


fun main() {
    val dictionary = loadDictionary()
    val featureOwner = FeatureOwnerWithDictionary(dictionary)
    val directory = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin"
    val documentStorage = DocumentStorage(directory, featureOwner)
    val value = "你好吗？我很好。春天来了，万物复苏"
    documentStorage.addDocument(value)
//    documentStorage.addDocument(ByteArray(10))

//    val keyword = "你"
//    val list = documentStorage.searchDocument(keyword)
//    list.forEach {
//        println("id:%s, score:%s".format(it.id, it.score))
//        val documentByteArray = documentStorage.collectDocument(it.id)
//        println(String(documentByteArray))
//    }
}