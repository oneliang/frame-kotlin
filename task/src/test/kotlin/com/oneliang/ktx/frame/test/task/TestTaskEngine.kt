package com.oneliang.ktx.frame.test.task

import com.oneliang.ktx.frame.task.TaskEngine
import com.oneliang.ktx.frame.task.TaskNode

fun main() {
    val taskEngine = TaskEngine(1, 2)
    val rootTaskNodeList = mutableListOf<TaskNode>()
    val taskNode1 = TaskNode().apply {
        this.name = "1"
        this.runnable = Runnable {
            Thread.sleep(1000)
            println("1 runnable")
        }
    }
    val taskNode2 = TaskNode().apply {
        this.name = "2"
        this.runnable = Runnable {
            Thread.sleep(1000)
            println("2 runnable")
        }
    }
    val taskNode3 = TaskNode().apply {
        this.name = "3"
        this.runnable = Runnable {
            println("3 runnable")
        }
    }
    taskNode1.addChildTaskNode(taskNode2)
    taskNode1.addChildTaskNode(taskNode3)
    rootTaskNodeList += taskNode1
    taskEngine.prepare(rootTaskNodeList)
    taskEngine.commit()
    taskEngine.start()
    taskEngine.await()
    taskEngine.stop()
}