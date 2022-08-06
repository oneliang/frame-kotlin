package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.util.math.segment.Segmenter

class PlanTime(var begin: Long, var end: Long) {
    constructor() : this(0L, 0L)

    init {
        if (end < begin) {
            error("begin[%s] bigger than end[%s] ".format(end, begin))
        } else {//end >= begin
            //ignore
        }
    }
}

fun List<PlanTime>.toSegmentList(): List<Segmenter.Segment<Any?>> {
    return this.map { Segmenter.Segment(begin = it.begin, end = it.end) }
}