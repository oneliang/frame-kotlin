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
    directory: String,
    private val featureOwner: FeatureOwner<String, Dictionary.WordCollector>,
    useCompress: Boolean = true
) : ContentStorage(directory, useCompress) {

    companion object {
        private val logger = LoggerManager.getLogger(DocumentStorage::class)
        private const val POINT_FILENAME = "point"
        private const val POINT_ID_MAPPING_FILENAME = "point_id_mapping"
    }

    private val point: SortedPoint

    private val pointIdMappingStorage: KeyValueStorage = KeyValueStorage(this.directory + Constants.Symbol.SLASH_LEFT + POINT_ID_MAPPING_FILENAME)
    private val pointIdAtomic: AtomicInteger
    private val edgeIdAtomic: AtomicInteger

    override val config: Config = Config()

    init {
        super.initialize()

        //point
        val pointFile = File(this.directory, POINT_FILENAME)
        this.point = SortedPoint(pointFile.absolutePath, this.useCompress)

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
        val documentId = this.addContent(data = value.toByteArray())
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
            this.point.add(pointWordCount.pointId, documentId, score)
            logger.verbose("word[%s], point id[%s], document id[%s], score[%s]".format(pointWordCount.value, pointWordCount.pointId, documentId, score))
        }

        relativeMap.forEach { (key, value) ->
            logger.verbose("edge[%s], document id[%s], score[%s]".format(key, value.first, Scorer.score(key.length, valueLength, 2.0)))
        }
        logger.verbose(pointIdWordList.toString())
        logger.verbose(relativeMap.toString())
        this.pointIdMappingStorage.save()
    }

    /**
     * search document
     * @param value
     * @return List<DocumentInfo>
     */
    fun searchDocument(value: String): List<DocumentInfo> {
        val wordCollector = this.featureOwner.extractFeature(value)
        val pointIdWordList = mutableListOf<Pair<Int, String>>()
        val documentInfoList = mutableListOf<DocumentInfo>()
        val documentInfoMap = mutableMapOf<Int, DocumentInfo>()
        val pointIdSet = hashSetOf<Int>()
        wordCollector.wordList.forEach {
            val (pointId, word) = getSuitablePointIdAndWord(it) ?: return@forEach//continue
            pointIdWordList += pointId to word
            if (!pointIdSet.contains(pointId)) {
                pointIdSet += pointId
                val list = this.point.find(pointId, 0, 10)
                logger.debug("point id:%s, word:%s, find size:%s", pointId, word, list.size)
                list.forEach { pointValueInfo ->
                    val newDocumentInfo = documentInfoMap.getOrPut(pointValueInfo.id) {
                        DocumentInfo(pointValueInfo.id, 0.0).apply {
                            documentInfoList += this
                        }
                    }
                    newDocumentInfo.totalScore += pointValueInfo.score
                }
            }
        }
        return documentInfoList.sortedByDescending { it.totalScore }
    }

    private class PointWordCount(val pointId: Int, val value: String, var count: Int = 0)

    class DocumentInfo(val documentId: Int, var totalScore: Double)

    @Mappable
    class Config : ContentStorage.Config() {
        companion object {
            private const val KEY_LAST_POINT_ID = "last_point_id"
            private const val KEY_LAST_EDGE_ID = "last_edge_id"
        }

        @Mappable.Key(KEY_LAST_POINT_ID)
        var lastPointId: Int = 0

        @Mappable.Key(KEY_LAST_EDGE_ID)
        var lastEdgeId: Int = 0
    }
}