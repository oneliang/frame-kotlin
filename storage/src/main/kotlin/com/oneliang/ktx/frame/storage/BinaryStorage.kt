package com.oneliang.ktx.frame.storage

import java.io.RandomAccessFile

class BinaryStorage(fullFilename: String, accessMode: AccessMode = AccessMode.RW) {

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
     * @return Pair<Long, Long>
     */
    fun write(data: ByteArray): Pair<Long, Long> {
        val start = this.file.length()
        this.file.seek(start)
        this.file.write(data)
        val end = this.file.length()
        return start to end
    }

    /**
     * finalize
     */
    fun finalize() {
        this.file.close()
    }
}