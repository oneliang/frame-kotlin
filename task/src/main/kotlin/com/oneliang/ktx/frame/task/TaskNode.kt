package com.oneliang.ktx.frame.task

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.common.perform
import com.oneliang.ktx.util.logging.LoggerManager
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock

class TaskNode {
    companion object {
        private val logger = LoggerManager.getLogger(TaskNode::class)
    }

    var name: String = Constants.String.BLANK
    var depth = 0
    var finished = false
    var runnable: Runnable? = null
    var runCostTime: Long = 0
    private val parentTaskNodeList: MutableList<TaskNode> = CopyOnWriteArrayList()
    private val childTaskNodeList: MutableList<TaskNode> = CopyOnWriteArrayList()
    private val lock = ReentrantLock()
    private val runCondition = lock.newCondition()
    /**
     * is all parent finished
     * @return boolean
     */
    val isAllParentFinished: Boolean
        get() {
            var allParentFinished = false
            var result = true
            for (parentTaskNode in parentTaskNodeList) {
                if (!parentTaskNode.finished) {
                    result = false
                    break
                }
            }
            if (result) {
                allParentFinished = true
            }
            return allParentFinished
        }

    /**
     * add parent task node
     * @param parentTaskNode
     */
    private fun addParentTaskNode(parentTaskNode: TaskNode) {
        if (!this.parentTaskNodeList.contains(parentTaskNode)) {
            this.parentTaskNodeList.add(parentTaskNode)
        }
    }

    /**
     * @return the parentTaskNodeList
     */
    fun getParentTaskNodeList(): List<TaskNode> {
        return this.parentTaskNodeList
    }

    /**
     * @return the childTaskNodeList
     */
    fun getChildTaskNodeList(): List<TaskNode> {
        return this.childTaskNodeList
    }

    /**
     * add child task node
     * @param childTaskNode
     */
    fun addChildTaskNode(childTaskNode: TaskNode) {
        if (!this.childTaskNodeList.contains(childTaskNode)) {
            this.childTaskNodeList.add(childTaskNode)
            childTaskNode.addParentTaskNode(this)
        }
    }

    /**
     * remove parent task node
     * @param parentTaskNode
     */
    fun removeParentTaskNode(parentTaskNode: TaskNode) {
        if (this.parentTaskNodeList.contains(parentTaskNode)) {
            this.parentTaskNodeList.remove(parentTaskNode)
            parentTaskNode.removeChildTaskNode(this)
        }
    }

    /**
     * remove child task node
     * @param childTaskNode
     */
    fun removeChildTaskNode(childTaskNode: TaskNode) {
        if (this.childTaskNodeList.contains(childTaskNode)) {
            this.childTaskNodeList.remove(childTaskNode)
            childTaskNode.removeParentTaskNode(this)
        }
    }

    /**
     * await, reentrant lock condition wait
     */
    fun await() {
        this.lock.lock()
        try {
            this.runCondition.await()
        } catch (e: InterruptedException) {
            logger.error(Constants.Base.EXCEPTION, e)
        } finally {
            this.lock.unlock()
        }
    }

    /**
     * signal, reentrant lock condition notify
     */
    fun signal() {
        this.lock.lock()
        try {
            this.runCondition.signal()
        } catch (e: InterruptedException) {
            logger.error(Constants.Base.EXCEPTION, e)
        } finally {
            this.lock.unlock()
        }
    }

    override fun toString(): String {
        return name
    }

    /**
     * clear parent task node list
     */
    fun clearParentTaskNodeList() {
        this.parentTaskNodeList.clear()
    }

    /**
     * clear child task node list
     */
    fun clearChildTaskNodeList() {
        this.childTaskNodeList.clear()
    }
}