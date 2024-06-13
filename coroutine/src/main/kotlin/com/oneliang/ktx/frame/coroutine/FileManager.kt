package com.oneliang.ktx.frame.coroutine

import kotlinx.coroutines.Job
import java.io.File

class FileManager(maxThreads: Int = Runtime.getRuntime().availableProcessors()) : AsyncProcessor(maxThreads) {

    companion object {
        internal const val SUFFIX_PROCESSING = ".processing"
        internal const val SUFFIX_SUCCESS = ".success"
    }


    /**
     * save file async
     */
    fun saveFileAsync(
        file: File,
        block: (file: File) -> Unit,
        exceptionCallback: suspend (e: Throwable, hashCode: Int) -> Boolean = { _, _ -> true },
    ): Job {
        val executeBlock: suspend () -> Unit = {
            saveFile(file, block)
        }
        return super.launch(executeBlock, exceptionCallback)
    }

    /**
     * save file
     */
    fun saveFile(
        file: File,
        block: (file: File) -> Unit
    ) {
        synchronized(file.absolutePath) {//lock for same file
            val processingFile = File(file.absolutePath + SUFFIX_PROCESSING)
            val successFile = File(file.absolutePath + SUFFIX_SUCCESS)
            //check file exist, will delete all processing file and success file
            if (processingFile.exists()) {
                processingFile.delete()
            }
            if (successFile.exists()) {
                successFile.delete()
            }
            //then create new processing file before execute
            if (!processingFile.exists()) {
                processingFile.createNewFile()
            }
            block(file)
            if (!successFile.exists()) {
                successFile.createNewFile()
                processingFile.delete()
            }
        }
    }
}

fun FileManager.Companion.checkFileProcessing(file: File): Boolean {
    val processingFile = File(file.absolutePath + SUFFIX_PROCESSING)
    return processingFile.exists()
}

fun FileManager.Companion.checkFileSuccess(file: File): Boolean {
    val successFile = File(file.absolutePath + SUFFIX_SUCCESS)
    return file.exists() && successFile.exists()
}