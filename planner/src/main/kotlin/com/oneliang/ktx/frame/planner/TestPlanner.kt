package com.oneliang.ktx.frame.planner

fun main() {
    val LINE_A = "A"
    val LINE_B = "B"
    val LINE_C = "C"
    val planLineGroupA = PlanLineGroup().also {
        it.key = LINE_A
        it.planLineList = listOf(PlanLine(LINE_A + "_1").apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        }, PlanLine(LINE_A + "_2").apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        })
    }
    val planLineGroupB = PlanLineGroup().also {
        it.key = LINE_B
        it.planLineList = listOf(PlanLine(LINE_B + "_1").apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        }, PlanLine(LINE_B + "_2").apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        })
    }
    val planLineGroupC = PlanLineGroup().also {
        it.key = LINE_C
        it.planLineList = listOf(PlanLine(LINE_C + "_1").apply {
            this.planTimeList = listOf(PlanTime(0, 1000L))
        }, PlanLine(LINE_C + "_2").apply {
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
    val planTaskA1 = PlanTask().also {
        it.key = "A1"
        it.stepList = listOf(PlanTask.Step(LINE_A).apply {
            this.planCostTime = 100
        })
    }
    val planTaskB1 = PlanTask().also {
        it.key = "B1"
        it.stepList = listOf(PlanTask.Step(LINE_B).apply {
            this.planCostTime = 200
        })
    }
    val planTaskC1 = PlanTask().also {
        it.key = "C1"
        it.stepList = listOf(PlanTask.Step(LINE_C).apply {
            this.planCostTime = 300
        })
    }
    val planLineGroupList = listOf(planLineGroupA, planLineGroupB, planLineGroupC)
    val planTaskList = listOf(planTaskABC, planTaskA, planTaskB, planTaskC, planTaskA1, planTaskB1, planTaskC1)
    Planner.plan(planLineGroupList, planTaskList)
    Planner.print(planLineGroupList)
}