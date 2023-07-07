package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.RandomAccessFile

class BinaryStorage(fullFilename: String, accessMode: AccessMode = AccessMode.RW) {
    companion object {
        private val logger = LoggerManager.getLogger(BinaryStorage::class)
    }

    internal val file: RandomAccessFile = RandomAccessFile(fullFilename, accessMode.value)

    enum class AccessMode(val value: String) {
        R("r"), RW("rw"), RWS("rws"), RWD("rwd")
    }

    /**
     * read
     * @param start
     * @param end
     * @return ByteArray
     */
    fun read(start: Long, end: Long): ByteArray {
        this.file.seek(start)
        val length = (end - start).toInt()
        val data = ByteArray(length)
        this.file.readFully(data, 0, length)
        return data
    }

    /**
     * write
     * @param data
     * @param startPosition, specify the start, use in some special business scene
     * @return Pair<Long, Long>
     */
    @Synchronized
    fun write(data: ByteArray, startPosition: Long = -1): Pair<Long, Long> {
        val start = if (startPosition > -1) {
            startPosition
        } else {
            this.file.length()
        }
        this.file.seek(start)
        this.file.write(data)
        val end = this.file.length()
        return start to end
    }

    /**
     * close
     */
    fun close() {
        try {
            this.file.close()
        } catch (e: Throwable) {
            logger.error(Constants.String.EXCEPTION, e)
        }
    }

    /**
     * finalize
     */
    fun finalize() {
        this.close()
    }
}