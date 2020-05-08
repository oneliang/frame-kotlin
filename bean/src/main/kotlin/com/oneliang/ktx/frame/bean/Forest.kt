package com.oneliang.ktx.frame.bean

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.ObjectUtil
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.CopyOnWriteArrayList

open class Forest<T : Any> {

    companion object {
        private val logger = LoggerManager.getLogger(Forest::class)
    }

    protected val treeList = CopyOnWriteArrayList<Tree<T>>()

    /**
     * create tree root list,like forest,many roots in the the list,but the root
     * has no child
     *
     * @param list
     * @param fartherFieldName
     * @param fartherValueSet
     * @return List<TreeNode></TreeNode><T>>
    </T> */
    protected fun createTreeRootList(list: List<T>, fartherFieldName: String, fartherValueSet: Array<Any>): List<TreeNode<T>> {
        val treeRootList = mutableListOf<TreeNode<T>>()
        try {
            for (instance in list) {
                if (fartherFieldName.isNotEmpty()) {
                    val fartherMethodName = ObjectUtil.fieldNameToMethodName(Constants.Method.PREFIX_GET, fartherFieldName)
                    val fartherMethod = instance.javaClass.getMethod(fartherMethodName)
                    val value = fartherMethod.invoke(instance)
                    if (Tree.checkValue(value, fartherValueSet)) {
                        val node = TreeNode(instance)
                        treeRootList.add(node)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(Constants.Base.EXCEPTION, e)
        }

        return treeRootList
    }

    /**
     * create the forest
     * @param list
     * @param fartherFieldName
     * @param fartherValueSet
     * @param childFieldName
     * @return List<Tree></Tree><T>>
    </T> */
    open fun createForest(list: List<T>, fartherFieldName: String, fartherValueSet: Array<Any>, childFieldName: String): List<Tree<T>> {
        val rootList = this.createTreeRootList(list, fartherFieldName, fartherValueSet)
        for (root in rootList) {
            val tree = Tree<T>()
            tree.createTree(root, list, fartherFieldName, fartherValueSet, childFieldName)
            this.treeList.add(tree)
        }
        return this.treeList
    }
}
