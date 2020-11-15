package com.oneliang.ktx.frame.task

import com.oneliang.ktx.Constants
import com.oneliang.ktx.exception.MethodNotSupportedException
import com.oneliang.ktx.util.common.toFile
import com.oneliang.ktx.util.concurrent.ThreadPool
import com.oneliang.ktx.util.concurrent.ThreadTask
import com.oneliang.ktx.util.file.FileUtil
import com.oneliang.ktx.util.file.createFileIncludeDirectory
import com.oneliang.ktx.util.logging.LoggerManager
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock

class TaskEngine(private val mode: Mode, minThreads: Int, maxThreads: Int) {
    companion object {
        private val logger = LoggerManager.getLogger(TaskEngine::class)
    }

    constructor(minThreads: Int, maxThreads: Int) : this(Mode.DEFAULT, minThreads, maxThreads) {}

    enum class Mode {
        DEFAULT, SERVER
    }

    var successful = true
    private var threadPool = ThreadPool()
    private var taskNodeTimeFullFilename = Constants.String.BLANK
    private var taskNodeTimeProperties: Properties? = null //default mode
    private var autoSort = false
    val defaultMode: Boolean
        get() = this.mode == Mode.DEFAULT

    val serverMode: Boolean
        get() = this.mode == Mode.SERVER
    private val lock = ReentrantLock()
    private val runCondition = lock.newCondition()
    private val allTaskNodeMap: MutableMap<String, TaskNode> = ConcurrentHashMap() //default mode
    private val taskNodeMapThreadLocal: ThreadLocal<MutableMap<String, TaskNode>> = object : ThreadLocal<MutableMap<String, TaskNode>>() {
        override fun initialValue(): MutableMap<String, TaskNode> {
            return mutableMapOf()
        }
    }
    private val processor: ThreadPool.Processor = object : ThreadPool.Processor {
        override fun beforeRunTaskProcess(threadTaskQueue: Queue<ThreadTask>) {
            val threadTask = threadTaskQueue.peek()
//            if (threadTask is TaskNode) {
//                val taskNode = threadTask as TaskNode
//            }
        }
    }

    init {
        this.threadPool.minThreads = minThreads
        this.threadPool.maxThreads = maxThreads
        this.threadPool.setProcessor(this.processor)
    }

    /**
     * prepare
     */
    fun prepare(rootTaskNodeList: List<TaskNode>, excludeTaskNodeNameList: List<String> = emptyList()) {
        travelAllTaskNode(rootTaskNodeList, excludeTaskNodeNameList)
    }

    /**
     * commit to thread pool
     */
    fun commit() {
        val taskNodeMap: Map<String, TaskNode> = taskNodeMapThreadLocal.get()
        val taskNodeDepthMap = mutableMapOf<Int, MutableList<TaskNode>>()
        val iterator = taskNodeMap.entries.iterator()
        val depthList = mutableListOf<Int>()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val taskNode = entry.value
            val depth = calculateTaskNodeDepth(taskNode)
            val taskNodeList = taskNodeDepthMap.getOrPut(depth) {
                depthList.add(depth)
                mutableListOf()
            }
            taskNodeList.add(taskNode)
        }
        val depthArray: Array<Int> = depthList.toTypedArray()
        Arrays.sort(depthArray)
        val hasAddThreadTaskMap = mutableMapOf<String, TaskNode>()
        for (depth in depthArray) {
            var taskNodeList: List<TaskNode> = taskNodeDepthMap[depth]!!
            if (this.autoSort) {
                taskNodeList = sort(taskNodeList)
            }
            for (taskNode in taskNodeList) {
                taskNode.depth = depth
                logger.info("task node depth:%s, task name:%s, cost time:%s", depth, taskNode.name, this.taskNodeTimeProperties?.getProperty(taskNode.name) ?: Constants.String.BLANK)
                if (!hasAddThreadTaskMap.containsKey(taskNode.name)) {
                    var multiTaskNodeThreadTask: MultiTaskNodeThreadTask? = null
                    val singleChildTaskNodeList = findSingleChildTaskNodeList(taskNode)
                    if (singleChildTaskNodeList.isNotEmpty()) {
                        multiTaskNodeThreadTask = MultiTaskNodeThreadTask()
                        multiTaskNodeThreadTask.addTaskNodeThreadTask(TaskNodeThreadTask(this, taskNode))
                        hasAddThreadTaskMap[taskNode.name] = taskNode
                        for (singleChildTaskNode in singleChildTaskNodeList) {
                            multiTaskNodeThreadTask.addTaskNodeThreadTask(TaskNodeThreadTask(this, singleChildTaskNode))
                            hasAddThreadTaskMap[singleChildTaskNode.name] = singleChildTaskNode
                        }
                        threadPool.addThreadTask(multiTaskNodeThreadTask)
                    }
                    if (multiTaskNodeThreadTask == null) {
                        hasAddThreadTaskMap[taskNode.name] = taskNode
                        threadPool.addThreadTask(TaskNodeThreadTask(this, taskNode))
                    }
                }
            }
        }
    }

    /**
     * find single child task node list
     * @param rootTaskNode
     * @return List<TaskNode>
    </TaskNode> */
    private fun findSingleChildTaskNodeList(rootTaskNode: TaskNode): List<TaskNode> {
        val singleChildTaskNodeList = mutableListOf<TaskNode>()
        val queue: Queue<TaskNode> = ConcurrentLinkedQueue()
        queue.add(rootTaskNode)
        while (!queue.isEmpty()) {
            val taskNode = queue.poll()
            val taskNodeChildTaskNodeList = taskNode.getChildTaskNodeList()
            if (taskNodeChildTaskNodeList.isNotEmpty() && taskNodeChildTaskNodeList.size == 1) {
                val childTaskNode = taskNodeChildTaskNodeList[0]
                val childTaskNodeParentTaskNodeList = childTaskNode.getParentTaskNodeList()
                if (childTaskNodeParentTaskNodeList.isNotEmpty() && childTaskNodeParentTaskNodeList.size == 1) {
                    singleChildTaskNodeList.add(childTaskNode)
                    queue.add(childTaskNode)
                }
            }
        }
        return singleChildTaskNodeList
    }

    /**
     * execute
     */
    fun start() {
        if (this.defaultMode) {
            if (this.allTaskNodeMap.isNotEmpty()) {
                this.threadPool.start()
            }
        } else {
            this.threadPool.start()
        }
    }

    /**
     * await, reentrant lock condition wait
     */
    fun await() {
        if (this.defaultMode) {
            if (this.allTaskNodeMap.isNotEmpty()) {
                this.lock.lock()
                try {
                    this.runCondition.await()
                } catch (e: InterruptedException) {
                    logger.error(Constants.Base.EXCEPTION, e)
                } finally {
                    this.lock.unlock()
                }
            }
        } else {
            throw MethodNotSupportedException("TaskEngine.waiting() is invalid for server mode.")
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

    /**
     * clean,clean all list and map cache,include thread pool interrupt
     */
    fun stop() {
        saveTaskNodeTime()
        this.threadPool.interrupt()
    }

    /**
     * travel all task node
     * @param rootTaskNodeList
     * @param excludeTaskNodeNameList
     * @return Map<String></String>,TaskNode>
     */
    private fun travelAllTaskNode(rootTaskNodeList: List<TaskNode>, excludeTaskNodeNameList: List<String> = emptyList()): Map<String, TaskNode> {
        val taskNodeMap = mutableMapOf<String, TaskNode>()
        val excludeTaskNodeNameMap = mutableMapOf<String, String>()
        for (excludeTaskNodeName in excludeTaskNodeNameList) {
            excludeTaskNodeNameMap[excludeTaskNodeName] = excludeTaskNodeName
        }
        val queue: Queue<TaskNode> = ConcurrentLinkedQueue()
        queue.addAll(rootTaskNodeList)
        val hasAddQueueMap = mutableMapOf<String, String>()
        while (!queue.isEmpty()) {
            val taskNode = queue.poll()
            val taskNodeName = taskNode.name
            if (!hasAddQueueMap.containsKey(taskNodeName)) {
                hasAddQueueMap[taskNodeName] = taskNodeName
            }
            if (!taskNodeMap.containsKey(taskNodeName) && !excludeTaskNodeNameMap.containsKey(taskNodeName)) {
                taskNodeMap[taskNodeName] = taskNode
                taskNodeMapThreadLocal.get()[taskNodeName] = taskNode
                if (defaultMode) {
                    allTaskNodeMap[taskNodeName] = taskNode
                }
            }
            val childTaskNodeList = taskNode!!.getChildTaskNodeList()
            if (childTaskNodeList.isNotEmpty()) {
                var result = false
                for (childTaskNode in childTaskNodeList) {
                    if (excludeTaskNodeNameMap.containsKey(childTaskNode.name)) {
                        val childParentTaskNodeList = childTaskNode.getParentTaskNodeList()
                        val childParentTempTaskNodeList = mutableListOf<TaskNode>()
                        //first delete the parent child relation,and keep the parent reference for last iterator
                        for (childParentTaskNode in childParentTaskNodeList) {
                            childParentTaskNode.removeChildTaskNode(childTaskNode)
                            childParentTempTaskNodeList.add(childParentTaskNode)
                        }
                        //second delete the child child relation,and keep the child child reference for last iterator
                        val childChildTaskNodeList = childTaskNode.getChildTaskNodeList()
                        val childChildTempTaskNodeList = mutableListOf<TaskNode>()
                        for (childChildTaskNode in childChildTaskNodeList) {
                            childChildTaskNode.removeParentTaskNode(childTaskNode)
                            childChildTempTaskNodeList.add(childChildTaskNode)
                        }
                        //then add the child child to parent
                        for (childParentTaskNode in childParentTempTaskNodeList) {
                            for (childChildTaskNode in childChildTempTaskNodeList) {
                                childParentTaskNode.addChildTaskNode(childChildTaskNode)
                            }
                        }
                        result = true
                    }
                }
                if (result) {
                    queue.add(taskNode) //recheck this task node
                }
            }
            for (childTaskNode in childTaskNodeList!!) {
                val childTaskNodeName = childTaskNode.name
                if (!taskNodeMap.containsKey(childTaskNodeName) && !excludeTaskNodeNameMap.containsKey(childTaskNodeName)) {
                    taskNodeMap[childTaskNodeName] = childTaskNode
                    taskNodeMapThreadLocal.get()[childTaskNodeName] = childTaskNode
                    if (defaultMode) {
                        allTaskNodeMap[childTaskNodeName] = childTaskNode
                    }
                }
                if (!hasAddQueueMap.containsKey(childTaskNodeName)) {
                    hasAddQueueMap[childTaskNodeName] = childTaskNodeName
                    queue.add(childTaskNode)
                }
            }
        }
        return taskNodeMap
    }

    private fun sort(taskNodeList: List<TaskNode>): List<TaskNode> {
        val taskNodeArray = taskNodeList.toTypedArray()
        for (i in taskNodeArray.indices) {
            val currentCostTime = taskNodeTimeProperties?.getProperty(taskNodeArray[i].name)?.toInt() ?: 0
            var maxCost = currentCostTime
            for (j in i until taskNodeArray.size) {
                val nextCostTime = taskNodeTimeProperties?.getProperty(taskNodeArray[j].name)?.toInt() ?: 0
                if (nextCostTime > maxCost) {
                    val temp = taskNodeArray[i]
                    taskNodeArray[i] = taskNodeArray[j]
                    taskNodeArray[j] = temp
                    maxCost = nextCostTime
                }
            }
        }
        return taskNodeArray.toList()
    }

    /**
     * calculate task node depth
     * @param taskNode
     * @return int
     */
    private fun calculateTaskNodeDepth(taskNode: TaskNode): Int {
        val depthList = mutableListOf<String>()
        val parentTaskNodeQueue: Queue<TaskNode> = ConcurrentLinkedQueue<TaskNode>()
        parentTaskNodeQueue.add(taskNode)
        while (!parentTaskNodeQueue.isEmpty()) {
            val parentTaskNode = parentTaskNodeQueue.poll()
            if (!depthList.contains(parentTaskNode.name)) {
                depthList.add(parentTaskNode.name)
            }
            for (parentParent in parentTaskNode.getParentTaskNodeList()) {
                if (!depthList.contains(parentParent.name)) {
                    parentTaskNodeQueue.add(parentParent)
                }
            }
        }
        return depthList.size
    }

    /**
     * is all task node finished
     * @return boolean
     */
    val isAllTaskNodeFinished: Boolean
        get() {
            var result = true
            if (this.defaultMode) {
                val iterator: Iterator<Map.Entry<String, TaskNode>> = this.allTaskNodeMap.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (!entry.value.finished) {
                        result = false
                    }
                }
            } else {
                throw RuntimeException("TaskEngine.isAllTaskNodeFinished() is invalid for server mode.")
            }
            return result
        }

    /**
     * save task node time
     */
    private fun saveTaskNodeTime() {
        if (this.taskNodeTimeProperties != null) {
            val taskNodeTimeFile = this.taskNodeTimeFullFilename.toFile()
            if (!taskNodeTimeFile.exists()) {
                taskNodeTimeFile.createFileIncludeDirectory()
            }
            val iterator: Iterator<Map.Entry<String, TaskNode>> = allTaskNodeMap.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val taskNode = entry.value
                this.taskNodeTimeProperties?.setProperty(taskNode.name, taskNode.runCostTime.toString())
            }
            try {
                this.taskNodeTimeProperties?.store(FileOutputStream(taskNodeTimeFullFilename), null)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     * @param taskNodeTimeFullFilename the taskNodeTimeFile to set
     */
    fun setTaskNodeTimeFullFilename(taskNodeTimeFullFilename: String) {
        this.taskNodeTimeFullFilename = taskNodeTimeFullFilename
        if (this.taskNodeTimeFullFilename.isNotBlank()) {
            try {
                val taskNodeTimeProperties = FileUtil.getPropertiesAutoCreate(this.taskNodeTimeFullFilename)
                this.taskNodeTimeProperties = taskNodeTimeProperties
                if (!taskNodeTimeProperties.isEmpty) {
                    autoSort = true
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}