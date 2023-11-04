package com.oneliang.ktx.frame.test.task

import com.oneliang.ktx.frame.task.TaskManager
import kotlinx.coroutines.delay
import java.util.*

fun main() {
    val taskManager = TaskManager()
    val task1 = object : TaskManager.Task() {
        override suspend fun runProcessor(processorIndex: Int) {
            println("task 1 run")
            delay(1000)
            println("task 1 run finish")
        }

        override suspend fun onCanceledProcessor(processorIndex: Int) {
            println("task 1 cancel")
        }

        override suspend fun onFinished() {
            println("task 1 finished")
        }
    }
    val task2 = object : TaskManager.Task() {
        override suspend fun runProcessor(processorIndex: Int) {
            println("task 2 run")
            delay(2000)
            println("task 2 run finish")
        }

        override suspend fun onCanceledProcessor(processorIndex: Int) {
            println("task 2 cancel")
        }

        override suspend fun onFinished() {
            println("task 2 finished")
        }
    }
    val task3 = object : TaskManager.Task() {
        override suspend fun runProcessor(processorIndex: Int) {
            println("task 3 run")
            delay(3000)
            println("task 3 run finish")
        }

        override suspend fun onCanceledProcessor(processorIndex: Int) {
            println("task 3 cancel")
        }

        override suspend fun onFinished() {
            println("task 3 finished")
        }
    }
    taskManager.startTask(task1)
    taskManager.startTask(task2)
//    taskManager.cancelAllTask()
    taskManager.startTask(task3, true)
    taskManager.restoreAllCancelledTask()

    val timer = Timer()
    timer.schedule(object : TimerTask() {
        override fun run() {
            taskManager.showTaskSize()
        }
    }, 0, 1000)
}