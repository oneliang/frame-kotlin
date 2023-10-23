package com.oneliang.ktx.frame.test.task

import com.oneliang.ktx.frame.task.TaskManager
import kotlinx.coroutines.delay

fun main() {
    val taskManager = TaskManager()
    val task1 = object : TaskManager.Task() {
        override suspend fun run() {
            println("task 1 run")
            delay(1000)
            println("task 1 run finish")
        }

        override suspend fun cancelCallback() {
            println("task 1 cancel")
        }
    }
    val task2 = object : TaskManager.Task() {
        override suspend fun run() {
            println("task 2 run")
            delay(2000)
            println("task 2 run finish")
        }

        override suspend fun cancelCallback() {
            println("task 2 cancel")
        }
    }
    val task3 = object : TaskManager.Task() {
        override suspend fun run() {
            println("task 3 run")
            delay(3000)
            println("task 3 run finish")
        }

        override suspend fun cancelCallback() {
            println("task 3 cancel")
        }
    }
    taskManager.startTask(task1)
    taskManager.startTask(task2)
//    taskManager.cancelAllTask()
    taskManager.startTask(task3, true)
}