package com.oneliang.ktx.frame.planner

import com.oneliang.ktx.util.math.segment.Segmenter

class PlanTime(var begin: Long, var end: Long) {

    init {
        if (end <= begin) {
            error("plan time end must be greater than begin, begin[%s], end[%s] ".format(begin, end))
        } else {//end >= begin
            //ignore
        }
    }
}

fun List<PlanTime>.toSegmentList(): List<Segmenter.Segment<Any?>> {
    return this.map { Segmenter.Segment(begin = it.begin, end = it.end) }
}