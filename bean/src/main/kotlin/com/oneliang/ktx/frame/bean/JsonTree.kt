package com.oneliang.ktx.frame.bean

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.json.JsonUtil

class JsonTree<T : Any> : Tree<T>() {

    /**
     * json tree,the name of has children field
     */
    var hasChildren = "hasChildren"
    /**
     * json tree ,the name of children's field
     */
    var children = "children"

    /**
     * json tree,key means the json's properties,value means the value of object's field
     * @param keyValueMap
     * @return String
     */
    fun generateJsonTree(keyValueMap: Map<String, String>): String {
        return generateJsonTree(this.root, keyValueMap, JsonUtil.DEFAULT_JSON_PROCESSOR)
    }

    /**
     * json tree,key means the json's properties,value means the value of object's field
     * @param keyValueMap
     * @param jsonProcessor
     * @return String
     */
    fun generateJsonTree(keyValueMap: Map<String, String>, jsonProcessor: JsonUtil.JsonProcessor): String {
        return generateJsonTree(this.root, keyValueMap, jsonProcessor)
    }

    /**
     * json tree,key means the json's properties,value means the value of object's field
     * @param root
     * @param keyValueMap
     * @param jsonProcessor
     * @return String
     */
    private fun generateJsonTree(root: TreeNode<T>, keyValueMap: Map<String, String>, jsonProcessor: JsonUtil.JsonProcessor = JsonUtil.DEFAULT_JSON_PROCESSOR): String {
        val stringBuilder = StringBuilder()
        val instance = root.instance
        val iterator = keyValueMap.entries.iterator()
        stringBuilder.append(Constants.Symbol.BIG_BRACKET_LEFT)
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val key = entry.key
            val fieldName = entry.value
            var methodReturnValue = ObjectUtil.getterOrIsMethodInvoke(instance, fieldName)
            methodReturnValue = jsonProcessor.process<Any>(null, fieldName, methodReturnValue, false)
            stringBuilder.append(Constants.Symbol.DOUBLE_QUOTES + key + Constants.Symbol.DOUBLE_QUOTES + Constants.Symbol.COLON + methodReturnValue.toString())
            if (iterator.hasNext()) {
                stringBuilder.append(Constants.Symbol.COMMA)
            }
        }
        if (!root.isLeaf) {
            val childList = root.childNodeList
            stringBuilder.append(Constants.Symbol.COMMA + Constants.Symbol.DOUBLE_QUOTES + hasChildren + Constants.Symbol.DOUBLE_QUOTES + Constants.Symbol.COLON + true)
            stringBuilder.append(Constants.Symbol.COMMA + Constants.Symbol.DOUBLE_QUOTES + children + Constants.Symbol.DOUBLE_QUOTES + Constants.Symbol.COLON + Constants.Symbol.MIDDLE_BRACKET_LEFT)
            var index = 0
            val lastIndex = childList.size - 1
            for (node in childList) {
                stringBuilder.append(generateJsonTree(node, keyValueMap, jsonProcessor))
                if (index < lastIndex) {
                    stringBuilder.append(Constants.Symbol.COMMA)
                }
                index++
            }
            stringBuilder.append(Constants.Symbol.MIDDLE_BRACKET_RIGHT)
        } else {
            stringBuilder.append(Constants.Symbol.COMMA + Constants.Symbol.DOUBLE_QUOTES + hasChildren + Constants.Symbol.DOUBLE_QUOTES + Constants.Symbol.COLON + false)
        }
        stringBuilder.append(Constants.Symbol.BIG_BRACKET_RIGHT)
        return stringBuilder.toString()
    }
}
