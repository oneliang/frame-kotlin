package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.search.FileStorage
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.Generator
import com.oneliang.ktx.util.json.jsonToArrayString
import com.oneliang.ktx.util.json.toJson
import java.io.File

class DefaultIndexReaderAndWriter(rootDirectory: String,
                                  private val indexKeyFilter: (key: String) -> Boolean = DEFAULT_INDEX_KEY_FILTER,
                                  private val dataKeyFilter: (key: String) -> Boolean = DEFAULT_DATA_KEY_FILTER) :
        IndexReader<String, String>,
        IndexWriter<Dictionary.WordCollector, DefaultIndexer.ValueItem> {

    companion object {
        val DEFAULT_INDEX_KEY_FILTER: (key: String) -> Boolean = {
            it != Constants.String.SPACE
                    && it != Constants.Symbol.SLASH_LEFT
                    && it != Constants.Symbol.SLASH_RIGHT
        }
        val DEFAULT_DATA_KEY_FILTER: (key: String) -> Boolean = {
            it.isNotBlank()
        }
        private const val INDEX_DIRECTORY_NAME = "index"
        private const val DATA_DIRECTORY_NAME = "data"
    }

    private val indexFileStorage: FileStorage
    private val dataFileStorage: FileStorage

    init {
        //index directory
        val indexDirectory = File(rootDirectory, INDEX_DIRECTORY_NAME).absolutePath
        this.indexFileStorage = FileStorage(indexDirectory)//, arrayOf(256, 256))
        this.indexFileStorage.initialize()
        //data directory
        val dataDirectory = File(rootDirectory, DATA_DIRECTORY_NAME).absolutePath
        this.dataFileStorage = FileStorage(dataDirectory)//, arrayOf(256, 256))
        this.dataFileStorage.initialize()
    }

    override fun write(key: Dictionary.WordCollector, value: DefaultIndexer.ValueItem) {
        val contentId = Generator.ID()
        val (indexMap, dataMap) = generateAllIndexMap(key.wordMap, contentId)
        val indexList = mutableListOf<Pair<String, String>>()
        val dataList = mutableListOf<Pair<String, String>>()
        //index
        indexMap.forEach { (key, contentIdSet) ->
            if (this.indexKeyFilter(key)) {
                indexList += key to contentIdSet.toJson()
            } else {
                return@forEach //continue
            }
        }
        this.indexFileStorage.addAll(indexList)
        //data
        dataMap.forEach { (key, contentIdSet) ->
            if (this.dataKeyFilter(key)) {
                dataList += key to contentIdSet.toJson()
            } else {
                return@forEach //continue
            }
        }
        this.dataFileStorage.addAll(dataList)

    }

    private fun generateAllIndexMap(wordMap: Map<String, List<Dictionary.Word>>, contentId: String): Pair<Map<String, Set<String>>, Map<String, Set<String>>> {
        val indexMap = mutableMapOf<String, MutableSet<String>>()
        val dataMap = mutableMapOf<String, MutableSet<String>>()
        generateAllIndexMap(wordMap, contentId, indexMap, dataMap)
        return indexMap to dataMap
    }

    private fun generateAllIndexMap(wordMap: Map<String, List<Dictionary.Word>>,
                                    contentId: String,
                                    indexMap: MutableMap<String, MutableSet<String>>,
                                    dataMap: MutableMap<String, MutableSet<String>>) {
        wordMap.forEach { (key, value) ->
            val wordContentIdSet = dataMap.getOrPut(key) { mutableSetOf() }
            wordContentIdSet += contentId

            var subKeySet = separateKeyword(key, 2)
            subKeySet.forEach {
                val valueSet = indexMap.getOrPut(it) { mutableSetOf() }
                valueSet += key
            }

            subKeySet = separateKeyword(key, 1)
            subKeySet.forEach {
                val valueSet = indexMap.getOrPut(it) { mutableSetOf() }
                valueSet += key
            }
        }
    }

    private fun separateKeyword(keyword: String, keywordSize: Int, block: (key: String) -> Unit = {}): Set<String> {
        if (keywordSize < 1) {
            error("separate keyword error, parameter[keywordSize] must be bigger than 0")
        }
        val keywordSet = mutableSetOf<String>()
        val charArray = keyword.toCharArray()
        var currentBeginIndex = 0
        val maxBeginIndex = keyword.length - keywordSize
        while (currentBeginIndex <= maxBeginIndex) {
            val key = charArray.copyOfRange(currentBeginIndex, currentBeginIndex + keywordSize).concatToString()
            keywordSet += key
            block(key)
            currentBeginIndex += 1
        }
        return keywordSet
    }

    override fun read(key: String): List<String> {
        val resultList = mutableListOf<String>()
        if (key.length == 1) {
            val list = this.indexFileStorage.search(key)
            for (item in list) {
                val jsonArray = item.jsonToArrayString()
                jsonArray.forEach {
                    resultList.add(it)
                }
            }
        } else {
            val subKeySet = separateKeyword(key, 2)
            for (subKey in subKeySet) {
                val list = this.indexFileStorage.search(subKey)
                for (item in list) {
                    val jsonArray = item.jsonToArrayString()
                    jsonArray.forEach {
                        resultList.add(it)
                    }
                }
            }
        }

        return resultList
    }
}