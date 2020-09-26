package com.oneliang.ktx.frame.planner

fun main() {
    val LINE_A = "A"
    val LINE_B = "B"
    val LINE_C = "C"
    val planLineGroupA = PlanLineGroup().also {
        it.key = LINE_A
        it.planLineList = listOf(PlanLine().apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        }, PlanLine().apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        })
    }
    val planLineGroupB = PlanLineGroup().also {
        it.key = LINE_B
        it.planLineList = listOf(PlanLine().apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        }, PlanLine().apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        })
    }
    val planLineGroupC = PlanLineGroup().also {
        it.key = LINE_C
        it.planLineList = listOf(PlanLine().apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        }, PlanLine().apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        })
    }
    val planTaskABC = PlanTask().also {
        it.key = "ABC"
        it.stepList = listOf(PlanTask.Step(LINE_A).apply {
            this.planCostTime = 100
        }, PlanTask.Step(LINE_B).apply {
            this.planCostTime = 200
        }, PlanTask.Step(LINE_C).apply {
            this.planCostTime = 300
        })
    }
    val planTaskA = PlanTask().also {
        it.key = "A"
        it.stepList = listOf(PlanTask.Step(LINE_A).apply {
            this.planCostTime = 100
        })
    }
    val planTaskB = PlanTask().also {
        it.key = "B"
        it.stepList = listOf(PlanTask.Step(LINE_B).apply {
            this.planCostTime = 200
        })
    }
    val planTaskC = PlanTask().also {
        it.key = "C"
        it.stepList = listOf(PlanTask.Step(LINE_C).apply {
            this.planCostTime = 300
        })
    }
    val planner = Planner()
    planner.planLineGroupList = listOf(planLineGroupA, planLineGroupB, planLineGroupC)
    planner.planTaskList = listOf(planTaskABC)//, planTaskA, planTaskB, planTaskC)
    planner.plan()
    planner.print()
}