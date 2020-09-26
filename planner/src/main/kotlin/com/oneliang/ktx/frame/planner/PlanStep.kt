package com.oneliang.ktx.frame.planner

class PlanStep(val planTask: PlanTask, val planTaskStep: PlanTask.Step) {
    var planBeginTime = 0L
    var planEndTime = 0L
}