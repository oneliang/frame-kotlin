package com.oneliang.ktx.frame.collector

abstract class HttpCacheCollector<T> : Collector<HttpCacheCollector.From, T> {
    class From(var httpUrl: String, var cacheDirectory: String)
}