package com.oneliang.ktx.frame.collector

interface Collector<FROM, DATA> {
    /**
     * collect
     *
     * @param from
     * @return DATA
     * @throws Exception
     */
    @Throws(Exception::class)
    fun collect(from: FROM): DATA
}