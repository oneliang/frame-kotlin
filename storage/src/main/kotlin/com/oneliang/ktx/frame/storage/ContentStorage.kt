package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.concurrent.locks.ReentrantLock

open class ContentStorage(
    protected val directory: String,
    protected val useCompress: Boolean = true
) {

    companion object {
        private val logger = LoggerManager.getLogger(ContentStorage::class)
        private const val SEGMENT_COUNT = 10
        private const val ROUTE_FILENAME = "route.ds"
        private const val CONFIG_FILENAME = "config"
        private const val SEGMENT_FILENAME_FORMAT = "segment_%s.ds"
        private const val INITIALIZE_FLAG = 1 shl 0
    }

    private val initializeLock = ReentrantLock()
    private lateinit var route: Route
    private lateinit var segmentMap: MutableMap<Short, SegmentInfo>
    private lateinit var circleIterator: CircleIterator<SegmentInfo>
    protected open val config: Config = Config()

    private val configManager: ConfigManager = ConfigManager()
    private val configFullFilename = this.directory + Constants.Symbol.SLASH_LEFT + CONFIG_FILENAME
    private var flag = 0

    /**
     * initialize
     */
    fun initialize() {
        if (this.flag.bitContains(INITIALIZE_FLAG)) {
            return
        }
        try {
            this.initializeLock.lock()
            //config
            this.configManager.readConfig(this.config, this.configFullFilename)
            //route
            this.directory.toFile().mkdirs()
            val routeFile = File(this.directory, ROUTE_FILENAME)
            this.route = Route(routeFile.absolutePath, initialValueId = this.config.lastId)

            //segment
            this.segmentMap = mutableMapOf()
            val segmentList = mutableListOf<SegmentInfo>()
            for (i in 0 until SEGMENT_COUNT) {
                val segmentNo = i.toShort()
                val segmentFile = File(this.directory, SEGMENT_FILENAME_FORMAT.format(segmentNo))
                val binaryStorage = if (segmentFile.exists()) {
                    BinaryStorage(segmentFile.absolutePath)
                } else {
                    null
                }
                val segmentInfo = SegmentInfo(segmentNo, binaryStorage)
                segmentList += segmentInfo
                this.segmentMap[segmentNo] = segmentInfo
            }
            val initialIndex = (this.config.lastSegmentNo + 1) % SEGMENT_COUNT
            this.circleIterator = CircleIterator(segmentList.toTypedArray(), initialIndex = initialIndex)
        } finally {
            this.flag = this.flag or INITIALIZE_FLAG
            this.initializeLock.unlock()
        }
    }

    /**
     * check initialize
     */
    protected fun checkInitialize() {
        if (!this.flag.bitContains(INITIALIZE_FLAG)) {
            initialize()
        }
    }

    /**
     * add content
     * @param data
     */
    open fun addContent(data: ByteArray): Int {
        checkInitialize()

        val segmentInfo = this.circleIterator.next()
        val segmentNo = segmentInfo.segmentNo
        var binaryStorage = segmentInfo.binaryStorage
        if (binaryStorage == null) {
            val segmentFile = File(this.directory, SEGMENT_FILENAME_FORMAT.format(segmentNo))
            binaryStorage = BinaryStorage(segmentFile.absolutePath)
            segmentInfo.binaryStorage = binaryStorage
        }

        val (start, end) = binaryStorage.write(compressData(data))
        val id = this.route.write(segmentNo, start, end)
        logger.info("add content finished, id[%s], segment no[%s], start[%s], end[%s]", id, segmentNo, start, end)
        this.config.lastId = id
        this.config.lastSegmentNo = segmentNo
        return id
    }

    /**
     * replace content
     * @param id
     * @param data
     * @return Boolean
     */
    open fun replaceContent(id: Int, data: ByteArray): Boolean {
        checkInitialize()

        val valueInfo = this.route.findValueInfo(id)
        if (valueInfo == null) {
            logger.error("id[%s] is not found".format(id))
            return false
        }
        val segmentInfo = this.segmentMap[valueInfo.segmentNo]
        if (segmentInfo == null) {
            logger.error("segment no[%s] is not found in segment map".format(valueInfo.segmentNo))
            return false
        }
        val binaryStorage = segmentInfo.binaryStorage
        if (binaryStorage == null) {
            logger.error("binary storage is null, please check the logic, segment no[%s]".format(valueInfo.segmentNo))
            return false
        }
        val compressedData = compressData(data)
        val dataOffset = compressedData.size - (valueInfo.end - valueInfo.start)
        binaryStorage.replace(valueInfo.start, valueInfo.end, compressedData)
        val updateSign = this.route.updateAllValueInfo(id, dataOffset)
        if (!updateSign) {
            logger.warning("update all value info is")
            return false
        }
        return true
    }

    /**
     * collection content
     * @param id
     * @return ByteArray
     */
    fun collectContent(id: Int): ByteArray {
        checkInitialize()

        val valueInfo = this.route.findValueInfo(id)
        if (valueInfo == null) {
            logger.error("id[%s] is not found".format(id))
            return ByteArray(0)
        }
        val segmentInfo = this.segmentMap[valueInfo.segmentNo]
        if (segmentInfo == null) {
            logger.error("segment no[%s] is not found in segment map".format(valueInfo.segmentNo))
            return ByteArray(0)
        }
        val binaryStorage = segmentInfo.binaryStorage
        if (binaryStorage == null) {
            logger.error("binary storage is null, please check the logic, segment no[%s]".format(valueInfo.segmentNo))
            return ByteArray(0)
        }
        return uncompressData(binaryStorage.read(valueInfo.start, valueInfo.end))
    }

    /**
     * collection content list
     * @param ids
     * @return List<ByteArray>
     */
    fun collectContentList(ids: Array<Int>): List<ByteArray> {
        checkInitialize()

        val list = mutableListOf<ByteArray>()
        for (id in ids) {
            list += this.collectContent(id)
        }
        return list
    }

    /**
     * compress data
     * @param value
     * @return ByteArray
     */
    protected fun compressData(value: ByteArray): ByteArray {
        return if (this.useCompress) {
            value.gzip()
        } else {
            value
        }
    }

    /**
     * uncompress data
     * @param value
     * @return ByteArray
     */
    protected fun uncompressData(value: ByteArray): ByteArray {
        return if (this.useCompress) {
            value.unGzip()
        } else {
            value
        }
    }

    open class SegmentInfo(var segmentNo: Short, var binaryStorage: BinaryStorage?)

    @Mappable
    open class Config {
        companion object {
            private const val KEY_LAST_ID = "last_id"
            private const val KEY_LAST_SEGMENT_NO = "last_segment_no"
        }

        @Mappable.Key(KEY_LAST_ID)
        var lastId: Int = 0

        @Mappable.Key(KEY_LAST_SEGMENT_NO)
        var lastSegmentNo: Short = -1
    }
}