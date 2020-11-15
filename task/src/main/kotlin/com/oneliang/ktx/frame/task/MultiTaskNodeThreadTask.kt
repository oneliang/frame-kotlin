package com.oneliang.ktx.frame.task

import com.oneliang.ktx.util.concurrent.ThreadTask
import java.util.concurrent.CopyOnWriteArrayList

class MultiTaskNodeThreadTask : ThreadTask {
    private val taskNodeThreadTaskList: MutableList<TaskNodeThreadTask> = CopyOnWriteArrayList()

    override fun runTask() {
        for (taskNodeThreadTask in this.taskNodeThreadTaskList) {
            taskNodeThreadTask.runTask()
        }
    }

    /**
     * add task node thread task
     * @param taskNodeThreadTask
     * @return boolean
     */
    fun addTaskNodeThreadTask(taskNodeThreadTask: TaskNodeThreadTask): Boolean {
        return taskNodeThreadTaskList.add(taskNodeThreadTask)
    }
}