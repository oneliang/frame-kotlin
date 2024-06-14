package com.oneliang.ktx.frame.parallel.processor

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.matchesBy
import com.oneliang.ktx.util.common.nullToBlank
import com.oneliang.ktx.util.common.transform
import com.oneliang.ktx.frame.parallel.ParallelTransformContext
import com.oneliang.ktx.frame.parallel.ParallelTransformProcessor

class MultiKeyValueMatchParallelTransformProcessor(private val keyValueArrayMap: Map<String, Array<String>>) : ParallelTransformProcessor<Pair<String, Map<String, String>>, MultiKeyValueMatchData> {
    override fun process(value: Pair<String, Map<String, String>>, parallelTransformContext: ParallelTransformContext<MultiKeyValueMatchData>) {
//    override fun flatMap(value: Tuple2<UNIQUE, Map<String, String>>, out: Collector<Tuple2<String, Int>>?) {
        val key = value.first
        if (key.isBlank()) {//last data for finish or error data
            parallelTransformContext.collect(EMPTY_MULTI_KEY_VALUE_MATCH_DATA)
            return
        }
        val dataMap = value.second
        val matchKeyList = dataMap.matchesBy(this.keyValueArrayMap)
        if (matchKeyList.isEmpty()) {
            parallelTransformContext.collect(EMPTY_MULTI_KEY_VALUE_MATCH_DATA)
            return
        }
        val multiKeyValueMatchData = matchKeyList.transform {
            MultiKeyValueMatchData().apply {
                val keyValueMatchMap = mutableMapOf<String, String>()
                val complexKey = it.joinToString(separator = Constants.Symbol.COMMA, postfix = Constants.Symbol.COMMA) { key ->
                    dataMap[key].nullToBlank().also {
                        keyValueMatchMap[key] = it
                    }
                }
                this.complexKey = complexKey
                this.keyValueMatchMap = keyValueMatchMap
                this.dataMap = dataMap
            }
        }
        parallelTransformContext.collect(multiKeyValueMatchData)
    }
}