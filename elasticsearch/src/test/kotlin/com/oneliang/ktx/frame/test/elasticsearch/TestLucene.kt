package com.oneliang.ktx.frame.test.elasticsearch

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.store.Directory
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

fun main() {
    val indexPath = "/Users/oneliang/Java/source/tinybar"
    println("Indexing to directory '$indexPath'...")
    val dir: Directory = FSDirectory.open(Paths.get(indexPath))
    val analyzer: Analyzer = StandardAnalyzer()
    val iwc = IndexWriterConfig(analyzer)
    val create = false
    if (create) {
        // Create a new index in the directory, removing any
        // previously indexed documents:
        iwc.openMode = OpenMode.CREATE
    } else {
        // Add new documents to an existing index:
        iwc.openMode = OpenMode.CREATE_OR_APPEND
    }
}