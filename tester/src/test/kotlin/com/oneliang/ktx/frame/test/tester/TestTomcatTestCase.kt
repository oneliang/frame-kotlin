package com.oneliang.ktx.frame.test.tester

import com.oneliang.ktx.frame.tester.TomcatTestCase

class TestTomcatTestCase : TomcatTestCase() {

    override fun test() {
        val baseUrl = this.baseUrlMap["aaa"]
        this.get("$baseUrl/monitorReporter/deviceStatus/onlineStatus.do")
    }
}