package com.oneliang.ktx.frame.storage

import com.oneliang.ktx.Constants
import com.oneliang.ktx.frame.feature.FeatureOwner
import com.oneliang.ktx.frame.tokenization.Dictionary
import com.oneliang.ktx.util.common.CircleIterator
import com.oneliang.ktx.util.common.Mappable
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
        private const val CONFIG_FILENAME = "config"
        private const val TERM_FILENAME = "term"
    }

    private val route: Route
    private val segmentMap: Map<Int, BinaryStorage>
    private val circleIterator: CircleIterator<SegmentInfo>
    private val config = Config()
    private val configManager: ConfigManager = ConfigManager()
    private val configFullFilename = this.directory + Constants.Symbol.SLASH_LEFT + CONFIG_FILENAME

    //    private val termFullFilename = this.directory + Constants.Symbol.SLASH_LEFT + TERM_FILENAME
    private val termStorage: KeyValueStorage = KeyValueStorage(this.directory + Constants.Symbol.SLASH_LEFT + TERM_FILENAME)
    private val termIdAtomic: AtomicInteger

    init {
        this.configManager.readConfig(this.config, this.configFullFilename)
        //route
        val routeFile = File(this.directory, "route.ds")
        this.route = Route(routeFile.absolutePath, initialDocumentId = this.config.lastDocumentId)

        //segment
        this.segmentMap = mutableMapOf()
        val segmentList = mutableListOf<SegmentInfo>()
        for (i in 0 until SEGMENT_COUNT) {
            segmentList += SegmentInfo(i.toShort(), null)
        }
        val initialIndex = (this.config.lastSegmentNo + 1) % SEGMENT_COUNT
        this.circleIterator = CircleIterator(segmentList.toTypedArray(), initialIndex = initialIndex)

        //term
        this.termIdAtomic = AtomicInteger(this.config.lastTermId)
    }

    /**
     * add document
     * @param value
     */
    fun addDocument(value: String) {
        val wordCollector = this.featureOwner.extractFeature(value)
        wordCollector.wordMap.forEach { (key, u) ->
            if (key.isBlank()) {
                return@forEach // continue
            }
            if (this.termStorage.hasProperty(key)) {
                this.termStorage.getProperty(key)
            } else {
                val termId = this.termIdAtomic.incrementAndGet()
                val termIdString = termId.toString()
                this.termStorage.setProperty(key, termIdString)
                println(termId)
                this.config.lastTermId = termId
            }
        }
//        addDocument(value.toByteArray())
    }

    /**
     * add document
     * @param data
     */
    private fun addDocument(data: ByteArray) {
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
    }

    private class SegmentInfo(var segmentNo: Short, var binaryStorage: BinaryStorage?)


    @Mappable
    private class Config {
        companion object {
            private const val KEY_LAST_DOCUMENT_ID = "last_document_id"
            private const val KEY_LAST_SEGMENT_NO = "last_segment_no"
            private const val KEY_LAST_TERM_ID = "last_term_id"
        }

        @Mappable.Key(KEY_LAST_DOCUMENT_ID)
        var lastDocumentId: Int = 0

        @Mappable.Key(KEY_LAST_SEGMENT_NO)
        var lastSegmentNo: Short = 0

        @Mappable.Key(KEY_LAST_TERM_ID)
        var lastTermId: Int = 0
    }
}