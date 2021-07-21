package com.oneliang.ktx.frame.adjuster

import com.oneliang.ktx.util.common.toMap
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager

object Adjuster {

    private val logger = LoggerManager.getLogger(Adjuster::class)

    fun forward(resourceList: List<Resource>, ruleList: List<Rule>): List<Result> {
        val ruleMap = mutableMapOf<String, Rule>()
        ruleList.forEach {
            if (ruleMap.containsKey(it.key)) {
                error("rule list have duplicate key, key:%s".format(it.key))
            }
            ruleMap[it.key] = it
        }
        val sortedRuleList = ruleList.sortedBy { it.order }

        val resultList = mutableListOf<Result>()
        for (resource in resourceList) {
            val begin = resource.begin
            val end = resource.end
            if (end <= begin) {
                error("end must bigger than end, begin:%s, end:%s".format(begin, end))
            }
            val totalCost = resource.end - resource.begin
            val itemList = resource.itemList
            val itemMap = resource.itemList.toMap { it.key to it }
            val itemListSize = itemList.size
            var minEnd = begin
            var maxEnd = begin
            var ruleMinEnd = begin
            var ruleMaxEnd = begin
            var stableCost = 0L
            val adjustRuleList = mutableListOf<Rule>()
            for (index in sortedRuleList.indices) {
                val rule = sortedRuleList[index]
                if (!itemMap.containsKey(rule.key)) {
                    adjustRuleList += rule
                    minEnd += rule.minCostTime
                    maxEnd += rule.maxCostTime
                    ruleMinEnd += rule.minCostTime
                    ruleMaxEnd += rule.maxCostTime
                } else {
                    val item = itemMap[rule.key] ?: error("key does not exist:%s".format(rule.key))
                    minEnd += item.costTime
                    maxEnd += item.costTime
                    stableCost += item.costTime
                }
            }
            if (minEnd > end || maxEnd > end) {
                error("resource end out of range, end:%s, min end:%s, max end:%s".format(end, minEnd, maxEnd))
            }
            val result = Result(resource, minEnd, maxEnd, stableCost)
            result.adjustRuleList = adjustRuleList
            resultList += result
        }
        return resultList
    }

    class Rule(val order: Int, val key: String, val minCostTime: Long, val maxCostTime: Long = minCostTime)

    class Item(val key: String, val costTime: Long)

    class Resource {
        var begin = 0L
        var end = 0L
        var itemList = emptyList<Item>()
    }

    class Result(val resource: Resource, val minEnd: Long, val maxEnd: Long, val stableCost: Long) {

        var adjustRuleList = emptyList<Rule>()
    }
}

fun main() {
    val ruleList = listOf(
        Adjuster.Rule(1, "K_A", 1, 2),
        Adjuster.Rule(2, "K_B", 2, 3),
        Adjuster.Rule(3, "K_C", 3, 5)
    )
    val resourceList = listOf(Adjuster.Resource().apply {
        this.begin = 10
        this.end = 20
        this.itemList = listOf(Adjuster.Item("K_B", 2))
    })
    val list = Adjuster.forward(resourceList, ruleList)
    println("forward result:%s".format(list.toJson()))
}