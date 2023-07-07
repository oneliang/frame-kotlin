package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.feature.FeatureOwner
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.*
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class DocumentStorage(
    private val directory: String,
    private val featureOwner: FeatureOwner<String, Dictionary.WordCollector>
) {

    companion object {
        private val logger = LoggerManager.getLogger(DocumentStorage::class)
        private const val SEGMENT_COUNT = 10
        private const val ROUTE_FILENAME = "route.ds"
        private const val CONFIG_FILENAME = "config"
        private const val POINT_FILENAME = "point"
        private const val EDGE_FILENAME = "edge"
    }

    private val route: Route
    private val segmentMap: Map<Int, BinaryStorage>
    private val circleIterator: CircleIterator<SegmentInfo>
    private val config = Config()
    private val configManager: ConfigManager = ConfigManager()
    private val configFullFilename = this.directory + Constants.Symbol.SLASH_LEFT + CONFIG_FILENAME

    private val pointStorage: KeyValueStorage = KeyValueStorage(this.directory + Constants.Symbol.SLASH_LEFT + POINT_FILENAME)
    private val pointIdAtomic: AtomicInteger
    private val edgeIdAtomic: AtomicInteger

    init {
        this.configManager.readConfig(this.config, this.configFullFilename)
        //route
        val routeFile = File(this.directory, ROUTE_FILENAME)
        this.route = Route(routeFile.absolutePath, initialDocumentId = this.config.lastDocumentId)

        //segment
        this.segmentMap = mutableMapOf()
        val segmentList = mutableListOf<SegmentInfo>()
        for (i in 0 until SEGMENT_COUNT) {
            segmentList += SegmentInfo(i.toShort(), null)
        }
        val initialIndex = (this.config.lastSegmentNo + 1) % SEGMENT_COUNT
        this.circleIterator = CircleIterator(segmentList.toTypedArray(), initialIndex = initialIndex)

        //point
        this.pointIdAtomic = AtomicInteger(this.config.lastPointId)

        //point
        this.edgeIdAtomic = AtomicInteger(this.config.lastEdgeId)
    }

    /**
     * add document
     * @param value
     */
    fun addDocument(value: String) {
        val wordCollector = this.featureOwner.extractFeature(value)
        val pointIdWordList = mutableListOf<Pair<String, String>>()
        wordCollector.wordList.forEach {
            val word = it.value
            if (it.value.length == 1 && it.value.toCharArray()[0].isSymbol()) {
                return@forEach//continue
            }
            if (word.isBlank()) {
                return@forEach // continue
            }
            val pointIdString = if (this.pointStorage.hasProperty(word)) {
                this.pointStorage.getProperty(word)
            } else {
                val pointId = this.pointIdAtomic.incrementAndGet()
                val pointIdString = pointId.toString()
                this.pointStorage.setProperty(word, pointIdString)
                this.config.lastPointId = pointId
                pointIdString
            }
            pointIdWordList += pointIdString to word
        }
        val documentId = addDocument(value.toByteArray())
        val relativeMap = pointIdWordList.toElementRelativeMap(keyTransform = {
            it.first
        }, valueTransform = {
            documentId
        })
        val valueLength = value.length
        pointIdWordList.forEach {
            println("word[%s], point id[%s], document id[%s], score[%s]".format(it.second, it.first, documentId, Scorer.score(it.second.length, valueLength)))
        }
        relativeMap.forEach { (key, value) ->
            println("edge[%s], document id[%s], score[%s]".format(key, value.first, Scorer.score(key.length, valueLength, 2.0)))
        }
        println(pointIdWordList)
        println(relativeMap)
        this.pointStorage.save()
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
            val segmentFile = File(this.directory, "segment_%s.ds".format(segmentNo))
            binaryStorage = BinaryStorage(segmentFile.absolutePath)
            segmentInfo.binaryStorage = binaryStorage
        }
        val (start, end) = binaryStorage.write(data)
        val documentId = this.route.write(segmentNo, start, end)
        logger.info("add document finished, document id[%s], segment no[%s], start[%s], end[%s]", documentId, segmentNo, start, end)
        this.config.lastDocumentId = documentId
        this.config.lastSegmentNo = segmentNo
        return documentId
    }

    private class SegmentInfo(var segmentNo: Short, var binaryStorage: BinaryStorage?)


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
        var lastSegmentNo: Short = 0

        @Mappable.Key(KEY_LAST_POINT_ID)
        var lastPointId: Int = 0

        @Mappable.Key(KEY_LAST_EDGE_ID)
        var lastEdgeId: Int = 0
    }
}