package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.file.FileWrapper
import com.oneliang.ktx.util.logging.LoggerManager

class BinaryStorage(private val fullFilename: String, private val accessMode: FileWrapper.AccessMode = FileWrapper.AccessMode.RW) {
    companion object {
        private val logger = LoggerManager.getLogger(BinaryStorage::class)
    }

    private val fileWrapper = FileWrapper(this.fullFilename, this.accessMode)


    /**
     * read
     * @param start
     * @param end
     * @return ByteArray
     */
    fun read(start: Long, end: Long): ByteArray {
        return this.fileWrapper.read(start, end)
    }

    /**
     * write
     * @param data
     * @param startPosition, specify the start, use in some special business scene
     * @return Pair<Long, Long>
     */
    fun write(data: ByteArray, startPosition: Long = -1): Pair<Long, Long> {
        return this.fileWrapper.write(data, startPosition)
    }

    /**
     * replace
     * @param start
     * @param end
     * @param data
     */
    fun replace(start: Long, end: Long, data: ByteArray) {
        this.fileWrapper.replace(start, end, data)
    }

    /**
     * length
     * @return Long
     */
    fun length(): Long {
        return this.fileWrapper.length()
    }

    /**
     * close
     */
    fun close() {
        this.fileWrapper.close()
    }

    /**
     * finalize
     */
    fun finalize() {
        this.close()
    }
}