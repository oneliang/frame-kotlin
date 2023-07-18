package com.oneliang.ktx.frame.test.search

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.coroutine.Coroutine
import com.oneliang.ktx.frame.storage.DocumentStorage
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.frame.tokenization.FeatureOwnerWithDictionary
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.jxl.readSimpleExcel
import com.oneliang.ktx.util.logging.*
import kotlinx.coroutines.Job
import java.io.FileOutputStream

private fun loadDictionary(): Dictionary {
    val dictionaryFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/main/resources/main.dic"
    val dictionary = Dictionary(DimensionSupportCategorizationStrategy())
    dictionary.load(dictionaryFullFilename)
    return dictionary
}

private fun initializeData(documentStorage: DocumentStorage) {
    val readResult = "/Users/oneliang/Java/test-data/steel_requirement_original_data.xls".toFile().readSimpleExcel(0)
    val begin = System.currentTimeMillis()
    val jobList = mutableListOf<Job>()
    val coroutine = Coroutine()
    coroutine.runBlocking {
        run loop@{
            readResult.dataList.forEachIndexed { index, data ->
//                if (index == 11) {
//                    return@loop //break
//                }
                val value = data["content"].nullToBlank()
                jobList += coroutine.launch {
                    documentStorage.addDocument(value)
                }
            }
        }

        jobList.forEach { it.join() }
        println("initialize data size:%s, cost:%s".format(readResult.dataList.size, System.currentTimeMillis() - begin))
    }
}

private fun searchData(documentStorage: DocumentStorage, value: String, outputFullFilename: String) {
    val writer = FileOutputStream(outputFullFilename.toFile()).writer()
    val begin = System.currentTimeMillis()
    val list = documentStorage.searchDocument(value)
    val end = System.currentTimeMillis()
    println("search cost:%s".format(end - begin))
    writer.use {
        list.forEachIndexed { index, documentInfo ->
            println("id:%s, score:%s".format(documentInfo.documentId, documentInfo.totalScore))
            val documentByteArray = documentStorage.collectContent(documentInfo.documentId)
            writer.write("----------%s----------".format(index + 1))
            writer.write(Constants.String.CRLF_STRING)
            writer.write(String(documentByteArray, Charsets.UTF_8))
            writer.write(Constants.String.CRLF_STRING)
//        String(documentByteArray)
        }
    }
}

fun main() {
    val directory = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/search/src/test/kotlin/storage"

    val loggerList = mutableListOf<AbstractLogger>()
//    loggerList += BaseLogger(Logger.Level.DEBUG)
    loggerList += FileLogger(Logger.Level.DEBUG, directory.toFile(), "default")
    val complexLogger = ComplexLogger(Logger.Level.INFO, loggerList, true)
    LoggerManager.registerLogger("*", complexLogger)

//    val file = File(directory, "route.ds")
//    println(file.readBytes().toHexString())
//    return
    val dictionary = loadDictionary()
    val featureOwner = FeatureOwnerWithDictionary(dictionary)

    val documentStorage = DocumentStorage(directory, featureOwner, true)
//    initializeData(documentStorage)
    documentStorage.changeToReadMode()
    searchData(documentStorage, "304 出售", directory + "/1_result.txt")
}