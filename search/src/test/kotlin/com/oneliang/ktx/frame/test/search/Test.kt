package com.oneliang.ktx.frame.test.search

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.search.engine.DefaultDataEngine
import com.oneliang.ktx.frame.search.engine.DefaultResourceReader
import com.oneliang.ktx.frame.search.engine.DefaultResourceWriter
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.jxl.readSimpleExcel
import java.io.BufferedReader
import java.io.InputStreamReader

private fun loadDictionary(): Dictionary {
    val dictionaryFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/main/resources/main.dic"
    val dictionary = Dictionary()
    dictionary.load(dictionaryFullFilename)
    return dictionary
}

fun migrateTest() {
    val dictionary = loadDictionary()
    val indexDirectory = "/Users/oneliang/Java/test-data/temp"
//    println(fileStorage.search("0.88").size)
//
//    return
    val featureOwner = FeatureOwnerForValueItem(dictionary)
    val resourceReader = DefaultResourceReader(indexDirectory)
    val resourceWriter = DefaultResourceWriter(indexDirectory)
    val defaultDataEngine = DefaultDataEngine(featureOwner, resourceReader, resourceWriter)
//    val dataFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/test/resources/data.json"
//    val content = StringBuilder()
//    dataFullFilename.toFile().readContentEachLine {
//        content.append(it.trim())
//        true
//    }
//    println(content)
//    val content = "求购201宏旺J1 0.5卷一个"
//    indexEngine.index(content.toString())
//    val readResult = "/Users/oneliang/Java/test-data/steel_requirement_original_data.xls".toFile().readSimpleExcel(0)
//    run loop@{
//        readResult.dataList.forEach {
//            val value = it["content"].nullToBlank()
//            defaultDataEngine.index(DefaultDataEngine.ValueItem(value, Constants.String.BLANK))
////            return@loop//break
//        }
//    }
    val inputStreamReader = InputStreamReader(System.`in`)
    val bufferedReader = BufferedReader(inputStreamReader)
    while (true) {
        val input = bufferedReader.readLine()
        val resultList = defaultDataEngine.search(input)
        println("***** Result *****")
        for ((index, result) in resultList.withIndex()) {
            println("%s:%s".format(index + 1, result))
            println("====================")
        }
    }
}

/**
 * data structure
 * {'漂亮':['漂亮可爱温柔的小姐姐'],'亮可':['漂亮可爱温柔的小姐姐']}
 */
private fun separateKeyword(keyword: String, block: (key: String) -> Unit = {}): Set<String> {
    val keywordSet = mutableSetOf<String>()
    val charArray = keyword.toCharArray()
    var beginIndex = 0
    val lastIndex = keyword.length - 1
    while (beginIndex < lastIndex) {
        val key = charArray.copyOfRange(beginIndex, beginIndex + 1).concatToString()
        keywordSet += key
        block(key)
        beginIndex += 1
    }
    return keywordSet
}

fun someTest() {
    val keywords = arrayOf(
            "漂亮可爱温柔的小哥哥",
            "漂亮可爱温柔的小姐姐"
    )
    val keyMap = mutableMapOf<String, MutableSet<String>>()
    for (keyword in keywords) {
        separateKeyword(keyword) { key ->
            val keywordSet = keyMap.getOrPut(key) { mutableSetOf() }
            keywordSet += keyword
        }
    }

    keyMap.forEach { (key, keywordSet) ->
        println("key:%s, keyword:%s".format(key, keywordSet))
    }
}

fun main() {
//    someTest()
    migrateTest()
}