package com.oneliang.ktx.frame.bean

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.json.JsonUtil

class JsonForest<T : Any> : Forest<T>() {

    /**
     * json tree,the name of has children field
     */
    var hasChildren = "hasChildren"
    /**
     * json forest ,the name of children's field
     */
    var children = "children"

    /**
     * create the forest
     * @param list
     * @param fartherFieldName
     * @param fartherValueSet
     * @param childFieldName
     * @return List<Tree></Tree><T>>
    </T> */
    override fun createForest(list: List<T>, fartherFieldName: String, fartherValueSet: Array<Any>, childFieldName: String): List<Tree<T>> {
        val rootList = this.createTreeRootList(list, fartherFieldName, fartherValueSet)
        for (root in rootList) {
            val tree = JsonTree<T>()
            tree.hasChildren = this.hasChildren
            tree.children = this.children
            tree.createTree(root, list, fartherFieldName, fartherValueSet, childFieldName)
            this.treeList.add(tree)
        }
        return this.treeList
    }

    /**
     * json forest,key means the json's properties,value means the value of object's field
     * @param keyValueMap
     * @param jsonProcessor
     * @return String
     */
    @JvmOverloads
    fun generateJsonForest(keyValueMap: Map<String, String>, jsonProcessor: JsonUtil.JsonProcessor = JsonUtil.DEFAULT_JSON_PROCESSOR): String {
        val string = StringBuilder()
        var index = 0
        val lastIndex = this.treeList.size - 1
        for (tree in this.treeList) {
            string.append((tree as JsonTree<T>).generateJsonTree(keyValueMap, jsonProcessor))
            if (index < lastIndex) {
                string.append(Constants.Symbol.COMMA)
            }
            index++
        }
        return Constants.Symbol.MIDDLE_BRACKET_LEFT + string.toString() + Constants.Symbol.MIDDLE_BRACKET_RIGHT
    }
}
/**
 * json forest,key means the json's properties,value means the value of object's field
 * @param keyValueMap
 * @return String
 */
