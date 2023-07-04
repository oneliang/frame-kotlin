package com.oneliang.ktx.frame.test.tokenization

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.Generator
import com.oneliang.ktx.util.common.toFillZeroString
import com.oneliang.ktx.util.file.saveTo
import com.oneliang.ktx.util.json.toJson


private fun generateAllIndexMap(wordMap: Map<String, List<Dictionary.Word>>,
                                contentId: String,
                                wordIndexMap: MutableMap<String, MutableSet<String>>,
                                charIndexMap: MutableMap<Char, MutableSet<String>>) {
    wordMap.forEach { (key, value) ->
        val wordContentIdSet = wordIndexMap.getOrPut(key) { mutableSetOf() }
        wordContentIdSet += contentId
//        println("${key}(${value.toJson()})")
        key.toCharArray().forEach {
            val charContentIdSet = charIndexMap.getOrPut(it) { mutableSetOf() }
            charContentIdSet += contentId
        }
    }
}

private fun search(keyword: String, wordIndexMap: Map<String, Set<String>>, charIndexMap: Map<Char, Set<String>>): Set<String> {
    val wordIndexContentIdSet = wordIndexMap[keyword]
    if (wordIndexContentIdSet != null) {
        return wordIndexContentIdSet
    } else {
        val allCharIndexContentIdSet = mutableSetOf<String>()
        keyword.toCharArray().forEach { it ->
            val charIndexContentIdSet = charIndexMap[it]
            if (charIndexContentIdSet != null) {
                allCharIndexContentIdSet += charIndexContentIdSet
            } else {
                return@forEach//continue
            }
        }
        return allCharIndexContentIdSet
    }
}

private fun testMemory() {
    val beginTime = System.currentTimeMillis()
    val beginHeapSize = Runtime.getRuntime().totalMemory()
    println(beginHeapSize)
    val beginHeapSizeM = beginHeapSize / Constants.Capacity.BYTES_PER_MB
    val map = mutableMapOf<String, String>()
    for (i in 1..1000000) {
        val value = i.toFillZeroString(20)
        map[value] = value
    }
    map.saveTo("/Users/oneliang/Java/temp/b", -1)
//    properties.saveTo("/Users/oneliang/Java/temp/a".toFile())
    val endHeapSize = Runtime.getRuntime().totalMemory()
    println(endHeapSize)
    val endHeapSizeM = endHeapSize / Constants.Capacity.BYTES_PER_MB
    val endTime = System.currentTimeMillis()
    println("size:%s, cost:%s, memory cost:%sM".format(map.size, endTime - beginTime, endHeapSizeM - beginHeapSizeM))
}

fun main() {
//    testMemory()
//    return

    val dictionaryFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/main/resources/main.dic"
//    val dictionaryFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/ik-analyzer/src/main/kotlin/com/oneliang/ktx/frame/ik/analyzer/dic/main2012.dic"
    val dictionary = Dictionary()
    dictionary.load(dictionaryFullFilename)
//    dictionary.loadDictionary(setOf("求购"))
//    val content = "求购201宏旺J1 0.5卷一个求购"
//    val content = "1.12 1.12 1.38/2.05*4今开0.92/0.95/1.42/1.35/0.68*4可加单"
    val content="你好吗？我很好。春天来了，万物复苏"
    val wordCollector = dictionary.splitWords(content)
    wordCollector.wordList.forEach { it ->
        println(it.value)
    }
    return
    val contentId = Generator.ID()
    val wordIndexMap = mutableMapOf<String, MutableSet<String>>()
    val charIndexMap = mutableMapOf<Char, MutableSet<String>>()
    generateAllIndexMap(wordCollector.wordMap, contentId, wordIndexMap, charIndexMap)
    println("--------------------")
    wordIndexMap.forEach { (key, value) ->
        println("${key}(${value.toJson()})")
    }
    return
    charIndexMap.forEach { (key, value) ->
        println("${key}(${value.toJson()})")
    }
    println("--------------------")
    val keyword = "求"
    val resultSet = search(keyword, wordIndexMap, charIndexMap)
    println(resultSet.toJson())

}