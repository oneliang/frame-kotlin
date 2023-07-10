package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.feature.FeatureOwner
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.pow

class DocumentStorage(
    private val directory: String,
    private val featureOwner: FeatureOwner<String, Dictionary.WordCollector>,
    private val useCompress: Boolean = true
) {

    companion object {
        private val logger = LoggerManager.getLogger(DocumentStorage::class)
        private const val SEGMENT_COUNT = 10
        private const val ROUTE_FILENAME = "route.ds"
        private const val CONFIG_FILENAME = "config"
        private const val POINT_FILENAME = "point"
        private const val POINT_ID_MAPPING_FILENAME = "point_id_mapping"
        private const val EDGE_FILENAME = "edge"
        private const val SEGMENT_FILENAME_FORMAT = "segment_%s.ds"
    }

    private val route: Route
    private val point: Point
    private val segmentMap: Map<Short, SegmentInfo>
    private val circleIterator: CircleIterator<SegmentInfo>
    private val config = Config()
    private val configManager: ConfigManager = ConfigManager()
    private val configFullFilename = this.directory + Constants.Symbol.SLASH_LEFT + CONFIG_FILENAME

    private val pointIdMappingStorage: KeyValueStorage = KeyValueStorage(this.directory + Constants.Symbol.SLASH_LEFT + POINT_ID_MAPPING_FILENAME)
    private val pointIdAtomic: AtomicInteger
    private val edgeIdAtomic: AtomicInteger

    init {
        this.configManager.readConfig(this.config, this.configFullFilename)
        //route
        val routeFile = File(this.directory, ROUTE_FILENAME)
        this.route = Route(routeFile.absolutePath, initialValueId = this.config.lastDocumentId)

        //point
        val pointFile = File(this.directory, POINT_FILENAME)
        this.point = Point(pointFile.absolutePath)

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

        //point
        this.pointIdAtomic = AtomicInteger(this.config.lastPointId)

        //point
        this.edgeIdAtomic = AtomicInteger(this.config.lastEdgeId)
    }

    /**
     * get suitable point id and word
     * @param dictionaryWord
     */
    private fun getSuitablePointIdAndWord(dictionaryWord: Dictionary.Word): Pair<Int, String>? {
        val word = dictionaryWord.value
        if (dictionaryWord.value.length == 1 && dictionaryWord.value.toCharArray()[0].isSymbol()) {
            return null
        }
        if (word.isBlank()) {
            return null
        }
        val pointId = if (this.pointIdMappingStorage.hasProperty(word)) {
            this.pointIdMappingStorage.getProperty(word).toInt()
        } else {
            val pointId = this.pointIdAtomic.incrementAndGet()
            val pointIdString = pointId.toString()
            this.pointIdMappingStorage.setProperty(word, pointIdString)
            this.config.lastPointId = pointId
            pointId
        }
        return pointId to word
    }

    /**
     * add document
     * @param value
     */
    fun addDocument(value: String) {
        val wordCollector = this.featureOwner.extractFeature(value)
        val pointIdWordList = mutableListOf<Pair<Int, String>>()
        val pointIdWordCountMap = mutableMapOf<Pair<Int, String>, PointWordCount>()
        wordCollector.wordList.forEach {
            val (pointId, word) = getSuitablePointIdAndWord(it) ?: return@forEach//continue
            pointIdWordList += pointId to word

            val pointWordCount = pointIdWordCountMap.getOrPut(Pair(pointId, word)) { PointWordCount(pointId, word) }
            pointWordCount.count++
        }
        val documentId = addDocument(value.toByteArray())
        val relativeMap = pointIdWordList.toElementRelativeMap(keyTransform = {
            it.first.toString()
        }, valueTransform = {
            documentId
        })
        val valueLength = value.length
//        pointIdWordList.forEach {
//            val score = Scorer.score(it.second.length, valueLength)
//            this.point.write(it.first, documentId, score)
//            println("word[%s], point id[%s], document id[%s], score[%s]".format(it.second, it.first, documentId, score))
//        }

        pointIdWordCountMap.forEach { (key, pointWordCount) ->
            val score = Scorer.score(pointWordCount.value.length, valueLength, 1.1.pow(pointWordCount.count))
            this.point.write(pointWordCount.pointId, documentId, score)
            println("word[%s], point id[%s], document id[%s], score[%s]".format(pointWordCount.value, pointWordCount.pointId, documentId, score))
        }

        relativeMap.forEach { (key, value) ->
            println("edge[%s], document id[%s], score[%s]".format(key, value.first, Scorer.score(key.length, valueLength, 2.0)))
        }
        println(pointIdWordList)
        println(relativeMap)
        this.pointIdMappingStorage.save()
    }

    /**
     * add document
     * @param data
     */
    private fun addDocument(data: ByteArray): Int {
        val segmentInfo = this.circleIterator.next()
        val segmentNo = segmentInfo.segmentNo
        var binaryStorage = segmentInfo.binaryStorage
        if (binaryStorage == null) {
            val segmentFile = File(this.directory, SEGMENT_FILENAME_FORMAT.format(segmentNo))
            binaryStorage = BinaryStorage(segmentFile.absolutePath)
            segmentInfo.binaryStorage = binaryStorage
        }

        val (start, end) = binaryStorage.write(compressData(data))
        val documentId = this.route.write(segmentNo, start, end)
        logger.info("add document finished, document id[%s], segment no[%s], start[%s], end[%s]", documentId, segmentNo, start, end)
        this.config.lastDocumentId = documentId
        this.config.lastSegmentNo = segmentNo
        return documentId
    }

    /**
     * compress data
     * @param value
     * @return ByteArray
     */
    private fun compressData(value: ByteArray): ByteArray {
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
    private fun uncompressData(value: ByteArray): ByteArray {
        return if (this.useCompress) {
            value.unGzip()
        } else {
            value
        }
    }

    /**
     * search document
     * @param value
     * @return List<Point.ValueInfo>
     */
    fun searchDocument(value: String): List<Point.ValueInfo> {
        val wordCollector = this.featureOwner.extractFeature(value)
        val pointIdWordList = mutableListOf<Pair<Int, String>>()
        wordCollector.wordList.forEach {
            val (pointId, word) = getSuitablePointIdAndWord(it) ?: return@forEach//continue
            pointIdWordList += pointId to word
        }
        val pointId = pointIdWordList[0].first
        return this.point.find(pointId)
    }

    /**
     * collection document
     * @param documentId
     * @return ByteArray
     */
    fun collectDocument(documentId: Int): ByteArray {
        val valueInfo = this.route.findValueInfo(documentId)
        if (valueInfo == null) {
            logger.error("document id[%s] is not found".format(documentId))
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
     * collection document list
     * @param documentIds
     * @return List<ByteArray>
     */
    fun collectDocumentList(documentIds: Array<Int>): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        for (documentId in documentIds) {
            list += this.collectDocument(documentId)
        }
        return list
    }

    private class SegmentInfo(var segmentNo: Short, var binaryStorage: BinaryStorage?)

    private class PointWordCount(val pointId: Int, val value: String, var count: Int = 0)

    @Mappable
    private class Config {
        companion object {
            private const val KEY_LAST_DOCUMENT_ID = "last_document_id"
            private const val KEY_LAST_SEGMENT_NO = "last_segment_no"
            private const val KEY_LAST_POINT_ID = "last_point_id"
            private const val KEY_LAST_EDGE_ID = "last_edge_id"
        }

        @Mappable.Key(KEY_LAST_DOCUMENT_ID)
        var lastDocumentId: Int = 0

        @Mappable.Key(KEY_LAST_SEGMENT_NO)
        var lastSegmentNo: Short = -1

        @Mappable.Key(KEY_LAST_POINT_ID)
        var lastPointId: Int = 0

        @Mappable.Key(KEY_LAST_EDGE_ID)
        var lastEdgeId: Int = 0
    }
}