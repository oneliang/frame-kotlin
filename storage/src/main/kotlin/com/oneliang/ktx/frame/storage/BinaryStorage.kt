package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.replace
import com.oneliang.ktx.util.concurrent.atomic.OperationLock
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.io.RandomAccessFile

class BinaryStorage(private val fullFilename: String, accessMode: AccessMode = AccessMode.RW) {
    companion object {
        private val logger = LoggerManager.getLogger(BinaryStorage::class)
    }

    internal val file: RandomAccessFile = RandomAccessFile(fullFilename, accessMode.value)
    private val readLock = OperationLock()
    private val writeLock = OperationLock()


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
        return this.readLock.operate {
            this.file.seek(start)
            val length = (end - start).toInt()
            val data = ByteArray(length)
            this.file.readFully(data, 0, length)
            data
        }

    }

    /**
     * write
     * @param data
     * @param startPosition, specify the start, use in some special business scene
     * @return Pair<Long, Long>
     */
    @Synchronized
    fun write(data: ByteArray, startPosition: Long = -1): Pair<Long, Long> {
        return this.writeLock.operate {
            val start = if (startPosition > -1) {
                startPosition
            } else {
                this.file.length()
            }
            this.file.seek(start)
            this.file.write(data)
            val end = this.file.length()
            start to end
        }
    }

    /**
     * replace
     * @param start
     * @param end
     * @param data
     */
    fun replace(start: Long, end: Long, data: ByteArray) {
        this.writeLock.operate {
            val file = File(this.fullFilename)
            file.replace(start, end, data, this.readLock.lock)
        }
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