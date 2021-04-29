package com.oneliang.ktx.frame.adjuster

import com.oneliang.ktx.util.common.calculateCompose
import com.oneliang.ktx.util.json.toJson
import com.oneliang.ktx.util.logging.LoggerManager
import kotlin.math.max

object Adjuster {

    private val logger = LoggerManager.getLogger(Adjuster::class)

    fun forward(resourceList: List<Resource>, ruleList: List<Rule>): List<ForwardResult> {
        val ruleMap = mutableMapOf<String, Rule>()
        ruleList.forEach {
            if (ruleMap.containsKey(it.key)) {
                error("rule list have duplicate key, key:%s".format(it.key))
            }
            ruleMap[it.key] = it
        }
        val sortedRuleList = ruleList.sortedBy { it.order }

        val forwardResultList = mutableListOf<ForwardResult>()
        for (resource in resourceList) {
            val begin = resource.begin
            val itemList = resource.itemList
            val itemListSize = itemList.size
            var end = begin
            var minEnd = 0L
            var maxEnd = 0L
            var forwardIndex = 0
            for (index in sortedRuleList.indices) {
                val rule = sortedRuleList[index]
                if (index < itemListSize) {
                    val item = itemList[index]
                    if (rule.key != item.key) {
                        error("key not match, did you miss the rule key:%s, rule order:%s, item key:%s, ".format(rule.key, rule.order, item.key))
                    }
                    end += item.costTime
                    minEnd = end
                    maxEnd = end
                    forwardIndex = index + 1//next index
                } else {
                    minEnd += rule.minCostTime
                    maxEnd += rule.maxCostTime
                }
            }
            val forwardResult = ForwardResult(resource, minEnd, maxEnd)
            forwardResult.adjustRuleList = sortedRuleList.subList(forwardIndex, sortedRuleList.size)
            forwardResultList += forwardResult
        }
        return forwardResultList
    }

    fun backward(resourceList: List<Resource>, ruleList: List<Rule>) {

    }

    class Rule(val order: Int, val key: String, val minCostTime: Long, val maxCostTime: Long = minCostTime)

    class Item(val key: String, val costTime: Long)

    class Resource {
        var begin = 0L
        var end = 0L
        var itemList = emptyList<Item>()
    }

    class ForwardResult(val resource: Resource, val minEnd: Long, val maxEnd: Long) {

        var adjustRuleList = emptyList<Rule>()
    }
}

fun main() {
    val ruleList = listOf(
        Adjuster.Rule(1, "K_A", 1),
        Adjuster.Rule(2, "K_B", 2),
        Adjuster.Rule(3, "K_C", 3)
    )
    val resourceList = listOf(Adjuster.Resource().apply {
        this.begin = 0
        this.end = 100
        this.itemList = listOf(Adjuster.Item("K_A", 10))
    })
    val list = Adjuster.forward(resourceList, ruleList)
    println("forward result:%s".format(list.toJson()))
}