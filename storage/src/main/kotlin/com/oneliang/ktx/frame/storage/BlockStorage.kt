package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.util.logging.LoggerManager

abstract class BlockStorage(
    fullFilename: String,
    accessMode: BinaryStorage.AccessMode = BinaryStorage.AccessMode.RW,
    val blockSize: Int
) {
    companion object {
        private val logger = LoggerManager.getLogger(BlockStorage::class)
    }

    private val binaryStorage = BinaryStorage(fullFilename, accessMode)

//    abstract val blockLength:Int

    init {
        initialize()
    }

    /**
     * initialize
     */
    private fun initialize() {
        val begin = System.currentTimeMillis()
        val fileLength = this.binaryStorage.file.length()

        assert(fileLength % this.blockSize == 0L)

        val blockCount = (fileLength / this.blockSize)
        logger.info("initialize, file length:%s, block size:%s, block count:%s", fileLength, this.blockSize, blockCount)
        for (i in 0 until blockCount) {
            val start = i * this.blockSize
            val end = (i + 1) * this.blockSize
            val byteArray = this.binaryStorage.read(start, end)
            this.readBlock(i.toInt(), start, byteArray)
        }
        logger.info("initialize cost:%s", System.currentTimeMillis() - begin)
    }

    /**
     * read block
     * @param index
     * @param start
     * @param byteArray
     */
    abstract fun readBlock(index: Int, start: Long, byteArray: ByteArray)

    /**
     * write anywhere
     * @param byteArray
     * @param startPosition
     * @return Pair<Long, Long>
     */
    fun write(byteArray: ByteArray, startPosition: Long = -1): Pair<Long, Long> {
        return this.binaryStorage.write(byteArray, startPosition)
    }

    /**
     * write block to file end
     * @param byteArray
     * @return Pair<Long, Long>
     */
    fun writeBlock(byteArray: ByteArray): Pair<Long, Long> {
        assert(byteArray.size == this.blockSize)
        return this.binaryStorage.write(byteArray)
    }


    /**
     * close
     */
    fun close() {
        this.binaryStorage.close()
    }

    /**
     * finalize
     */
    fun finalize() {
        this.binaryStorage.finalize()
    }
}