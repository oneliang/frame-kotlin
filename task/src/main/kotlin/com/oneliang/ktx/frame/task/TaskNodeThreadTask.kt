package com.oneliang.ktx.frame.task

import com.oneliang.ktx.Constants
import com.oneliang.ktx.util.concurrent.ThreadTask
import com.oneliang.ktx.util.logging.LoggerManager

class TaskNodeThreadTask(private val taskEngine: TaskEngine, private val taskNode: TaskNode) : ThreadTask {
    companion object {
        private val logger = LoggerManager.getLogger(TaskNodeThreadTask::class)
    }

    override fun runTask() {
        val begin = System.currentTimeMillis()
        logger.info("task name:%s ready", this.taskNode.name)
        while (!this.taskNode.isAllParentFinished) {
            this.taskNode.await()
        }
        val runBegin = System.currentTimeMillis()
        try {
            val runnable = taskNode.runnable
            if (runnable != null) {
                logger.info("task name:%s start", taskNode.name)
                runnable.run()
            }
        } catch (e: Exception) {
            logger.error(Constants.String.EXCEPTION, e)
            this.taskEngine.successful = false
        } finally {
            this.taskNode.finished = true
        }
        val runCost = System.currentTimeMillis() - runBegin
        if (this.taskNode.getChildTaskNodeList().isNotEmpty()) {
            for (childTaskNode in taskNode.getChildTaskNodeList()) { //				taskEngine.executeTaskNode(childTaskNode);
                childTaskNode.signal()
            }
        }
        val taskCost = System.currentTimeMillis() - begin
        logger.info("task name:%s end, task cost:%s, run cost:%s ,waiting:%s", taskNode.name, taskCost, runCost, taskCost - runCost)
        this.taskNode.runCostTime = runCost
        if (this.taskEngine.defaultMode) {
            if (this.taskEngine.isAllTaskNodeFinished) {
                this.taskEngine.signal()
            }
        }
    }
}