package com.oneliang.ktx.frame.test.storage

import com.oneliang.ktx.frame.storage.ConfigManager
import com.oneliang.ktx.util.common.Mappable

@Mappable
class TestConfig {

    @Mappable.Key("last_document_id")
    var lastDocumentId: Int = 0

    @Mappable.Key("last_segment_no")
    var lastSegmentNo: Int = 0
}

fun main() {
    val testConfig = TestConfig()
    val configManager = ConfigManager()
    val configFullFilename = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/storage/src/test/kotlin/config"
    configManager.readConfig(testConfig, configFullFilename)

    testConfig.lastDocumentId = 10
    testConfig.lastSegmentNo = 5
    configManager.writeConfig(testConfig)
}