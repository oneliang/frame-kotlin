package com.oneliang.ktx.frame.bean

import java.util.concurrent.CopyOnWriteArrayList

/**
 * class menu node like tree node
 * @author Dandelion
 * @since 2008-10-17
 */
class TreeNode<T : Any>(val instance: T) {

    /**
     * @return the fartherNode
     */
    /**
     * @param fartherNode the fartherNode to set
     */
    var fartherNode: TreeNode<T>? = null
    /**
     * @return the object
     */
    /**
     * @param object the object to set
     */
    val childNodeList = CopyOnWriteArrayList<TreeNode<T>>()
    /**
     * @return the depth
     */
    /**
     * @param depth the depth to set
     */
    var depth = 0

    /**
     * the tree node is root or not
     * @return true or false
     */
    val isRoot: Boolean
        get() {
            var result = false
            if (this.fartherNode == null) {
                result = true
            }
            return result
        }

    /**
     * the tree node is leaf or not
     * @return true or false
     */
    val isLeaf: Boolean
        get() {
            var result = false
            if (this.childNodeList.isEmpty()) {
                result = true
            }
            return result
        }

    /**
     *
     * Method: add a TreeNode
     * @param treeNode
     * @return this.TreeNode<T>
    </T> */
    fun addTreeNode(treeNode: TreeNode<T>): TreeNode<T> {
        if (this == treeNode) {
            throw RuntimeException("it can not add itself!")
        }
        childNodeList.add(treeNode)
        return this
    }

    /**
     *
     * Method: remove a TreeNode
     * @param treeNode
     * @return this.TreeNode<T>
    </T> */
    fun removeTreeNode(treeNode: TreeNode<T>): TreeNode<T> {
        if (this == treeNode) {
            throw RuntimeException("it can not remove itself!")
        }
        childNodeList.remove(treeNode)
        return this
    }
}
