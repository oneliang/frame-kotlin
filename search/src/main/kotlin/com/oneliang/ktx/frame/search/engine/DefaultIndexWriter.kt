package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.search.FileStorage
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.Generator
import com.oneliang.ktx.util.json.toJson

class DefaultIndexWriter(private val fileStorage: FileStorage) : IndexWriter<Dictionary.WordCollector, String> {

    fun initialize() {
        this.fileStorage.initialize()
    }

    override fun writeIndex(key: Dictionary.WordCollector, value: String) {
        val (wordIndexMap, charIndexMap) = generateAllIndexMap(key.wordMap)
        val list = mutableListOf<Pair<String, String>>()
        wordIndexMap.forEach { (key, contentIdSet) ->
            if (key.isBlank()) {
                return@forEach //continue
            }
            list += key to contentIdSet.toJson()
        }
        charIndexMap.forEach { (key, contentIdSet) ->
            if (key == Constants.Ascii.SPACE) {
                return@forEach //continue
            }
            list += key.toString() to contentIdSet.toJson()
        }

        this.fileStorage.addAll(list)
    }

    fun destroy() {
        this.fileStorage.destroy()
    }

    private fun generateAllIndexMap(wordMap: Map<String, List<Dictionary.Word>>): Pair<Map<String, Set<String>>, Map<Char, Set<String>>> {
        val wordIndexMap = mutableMapOf<String, MutableSet<String>>()
        val charIndexMap = mutableMapOf<Char, MutableSet<String>>()
        val contentId = Generator.ID()
        generateAllIndexMap(wordMap, contentId, wordIndexMap, charIndexMap)
        return wordIndexMap to charIndexMap
    }

    private fun generateAllIndexMap(wordMap: Map<String, List<Dictionary.Word>>,
                                    contentId: String,
                                    wordIndexMap: MutableMap<String, MutableSet<String>>,
                                    charIndexMap: MutableMap<Char, MutableSet<String>>) {
        wordMap.forEach { (key, value) ->
            val wordContentIdSet = wordIndexMap.getOrPut(key) { mutableSetOf() }
            wordContentIdSet += contentId
            key.toCharArray().forEach {
                val charContentIdSet = charIndexMap.getOrPut(it) { mutableSetOf() }
                charContentIdSet += contentId
            }
        }
    }
}