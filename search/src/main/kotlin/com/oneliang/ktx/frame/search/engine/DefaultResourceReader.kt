package com.oneliang.ktx.frame.search.engine

import com.oneliang.ktx.frame.search.DataStorage
import com.oneliang.ktx.util.common.extractKeyword
import com.oneliang.ktx.util.json.jsonToArrayString
import java.io.File

class DefaultResourceReader(rootDirectory: String) : ResourceReader<String, String> {

    companion object {
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

    override fun read(key: String): List<String> {
        val resultList = mutableListOf<String>()
        if (key.length == 1) {
            val list = this.indexDataStorage.search(key)
            for (item in list) {
                val jsonArray = item.jsonToArrayString()
                jsonArray.forEach {
                    resultList.add(it)
                }
            }
        } else {
            val subKeySet = key.extractKeyword(2)
            for (subKey in subKeySet) {
                val list = this.indexDataStorage.search(subKey)
                for (item in list) {
                    resultList.add(item)
                }
            }
        }

        return resultList
    }
}