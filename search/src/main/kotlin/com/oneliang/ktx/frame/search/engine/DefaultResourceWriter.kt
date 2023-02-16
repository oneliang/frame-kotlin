package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.search.DataStorage
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.Generator
import com.oneliang.ktx.util.common.extractKeyword
import java.io.File

class DefaultResourceWriter(rootDirectory: String,
                            private val indexKeyFilter: (key: String) -> Boolean = DEFAULT_INDEX_KEY_FILTER,
                            private val valueKeyFilter: (key: String) -> Boolean = DEFAULT_DATA_KEY_FILTER) :
        ResourceWriter<Dictionary.WordCollector, DefaultDataEngine.ValueItem> {

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
        private const val VALUE_DIRECTORY_NAME = "value"
    }

    private val indexDataStorage: DataStorage
    private val valueDataStorage: DataStorage

    init {
        //index directory
        val indexDirectory = File(rootDirectory, INDEX_DIRECTORY_NAME).absolutePath
        this.indexDataStorage = DataStorage(indexDirectory)//, arrayOf(256, 256))
        this.indexDataStorage.initialize()
        //value directory
        val valueDirectory = File(rootDirectory, VALUE_DIRECTORY_NAME).absolutePath
        this.valueDataStorage = DataStorage(valueDirectory)//, arrayOf(256, 256))
        this.valueDataStorage.initialize()
    }

    override fun write(key: Dictionary.WordCollector, value: DefaultDataEngine.ValueItem) {
        val contentId = Generator.ID()
        val (indexMap, valueMap) = generateAllIndexMap(key.wordMap, contentId)
        val indexList = mutableListOf<Pair<String, String>>()
        val valueList = mutableListOf<Pair<String, String>>()
        //index
        indexMap.forEach { (subKey, keySet) ->
            if (this.indexKeyFilter(subKey)) {
                for (indexKey in keySet) {
                    indexList += subKey to indexKey
                }
            } else {
                return@forEach //continue
            }
        }
        this.indexDataStorage.addAll(indexList)
        //data
        valueMap.forEach { (key, valueSet) ->
            if (this.valueKeyFilter(key)) {
                for (value in valueSet) {
                    valueList += key to value
                }
            } else {
                return@forEach //continue
            }
        }
        this.valueDataStorage.addAll(valueList)

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

            var subKeySet = key.extractKeyword(2)
            subKeySet.forEach {
                val valueSet = indexMap.getOrPut(it) { mutableSetOf() }
                valueSet += key
            }

            subKeySet = key.extractKeyword(1)
            subKeySet.forEach {
                val valueSet = indexMap.getOrPut(it) { mutableSetOf() }
                valueSet += key
            }
        }
    }
}