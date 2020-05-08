package com.oneliang.ktx.frame.bean

import com.oneliang.ktx.util.common.ObjectUtil
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

open class Tree<T : Any> {
    companion object {
        /**
         * private method: check the fieldvalue is values or not
         *
         * @param fieldValue
         * @param values
         * @return boolean
         */
        internal fun checkValue(fieldValue: Any, values: Array<Any>): Boolean {
            var sign = false
            for (value in values) {
                if (fieldValue == value) {
                    sign = true
                }
            }
            return sign
        }
    }

    /**
     * @return the root
     */
    lateinit var root: TreeNode<T>
        protected set

    /**
     * create tree,the tree has the child node
     * childFieldName.value==fartherFieldName.value prove the child
     *
     * @param list
     * @param fartherFieldName
     * @param fartherValueSet
     * @param childFieldName
     * @return the tree node has the child
     */
    fun createTree(farther: TreeNode<T>, list: List<T>, fartherFieldName: String, fartherValueSet: Array<Any>, childFieldName: String): TreeNode<T> {
        this.root = farther
        val copyOnWriteList = CopyOnWriteArrayList(list)
        val queue = ConcurrentLinkedQueue<TreeNode<T>>()
        queue.add(farther)
        while (!queue.isEmpty()) {
            val treeNode = queue.poll()
            val fartherObject = treeNode.instance
            for (instance in copyOnWriteList) {
                // parentId,id
                val fartherIdValue = ObjectUtil.getterOrIsMethodInvoke(fartherObject, childFieldName)
                val childParentValue = ObjectUtil.getterOrIsMethodInvoke(instance, fartherFieldName)
                if (!checkValue(childParentValue, fartherValueSet)) {// prove child
                    if (childParentValue == fartherIdValue) {
                        val node = TreeNode(instance)
                        node.fartherNode = treeNode
                        node.depth = treeNode.depth + 1
                        treeNode.addTreeNode(node)
                        queue.add(node)
                        copyOnWriteList.remove(instance)
                    }
                }
            }
        }
        return farther
    }
}
