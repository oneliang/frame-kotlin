package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.util.math.segment.Segmenter

class PlanTime(var begin: Long, var end: Long) {
    constructor() : this(0L, 0L)
}

fun List<PlanTime>.toSegmentList(): List<Segmenter.Segment<Any?>> {
    return this.map { Segmenter.Segment<Any?>(begin = it.begin, end = it.end) }
}