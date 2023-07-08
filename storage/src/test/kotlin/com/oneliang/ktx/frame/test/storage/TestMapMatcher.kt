package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toElementRelativeMap

private fun loadDictionary(): Dictionary {
    val dictionaryFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/main/resources/main.dic"
    val dictionary = Dictionary()
    dictionary.load(dictionaryFullFilename)
    return dictionary
}

fun main() {
    val dictionary = loadDictionary()
    dictionary.addKeywordToDictionary("万物复苏")
    val documentContent = "你好吗？我很好。春天来了，万物复苏"

    val wordCollector = dictionary.splitWords(documentContent)
    wordCollector.wordList.forEach { it ->
        println(it.value)
    }
    return
    val termMap = mapOf(
        "好吗" to 1,
        "很好" to 2,
        "春天" to 3,
        "来了" to 4,
        "万物复苏" to 5
    )
    val termIdMap = mapOf(
        1 to "好吗",
        2 to "很好",
        3 to "春天",
        4 to "来了",
        5 to "万物复苏"
    )
    val searchContent = "好吗"
    val documentId = 1
    val documentPointMap = mapOf(
        1 to (documentId to termIdMap[1].nullToBlank().length.toDouble() / documentContent.length),
        2 to (documentId to termIdMap[2].nullToBlank().length.toDouble() / documentContent.length),
        3 to (documentId to termIdMap[3].nullToBlank().length.toDouble() / documentContent.length),
        4 to (documentId to termIdMap[4].nullToBlank().length.toDouble() / documentContent.length),
        5 to (documentId to termIdMap[5].nullToBlank().length.toDouble() / documentContent.length)

    )
    val documentEdgeMap = mapOf(
        "%s->%s".format(1, 2) to (documentId to 2 * (termIdMap[1].nullToBlank().length.toDouble() + termIdMap[2].nullToBlank().length.toDouble()) / documentContent.length),
        "%s->%s".format(2, 3) to (documentId to 2 * (termIdMap[2].nullToBlank().length.toDouble() + termIdMap[3].nullToBlank().length.toDouble()) / documentContent.length),
        "%s->%s".format(3, 4) to (documentId to 2 * (termIdMap[3].nullToBlank().length.toDouble() + termIdMap[4].nullToBlank().length.toDouble()) / documentContent.length),
        "%s->%s".format(4, 5) to (documentId to 2 * (termIdMap[4].nullToBlank().length.toDouble() + termIdMap[5].nullToBlank().length.toDouble()) / documentContent.length)
    )
    println(documentPointMap)
    println(documentEdgeMap)

    val searchContentTerms = arrayOf("好吗", "万物复苏")
    val searchPointList = mutableListOf<Int>()
    val searchEdgeMap = searchContentTerms.map {
        val termId = termMap[it] ?: 0
        searchPointList += termId
        termId
    }.toElementRelativeMap()
    val searchPointSum = searchPointList.sumOf {
        documentPointMap[it]?.second ?: 0.0
    }
    var searchEdgeSum = 0.0
    searchEdgeMap.forEach { (edgeKey, u) ->
        println(edgeKey)
        searchEdgeSum += documentEdgeMap[edgeKey]?.second ?: 0.0
    }
    println(searchPointSum)
    println(searchEdgeSum)
}