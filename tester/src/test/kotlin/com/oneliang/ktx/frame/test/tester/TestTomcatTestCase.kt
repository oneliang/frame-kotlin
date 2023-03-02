package com.oneliang.ktx.frame.test.tester

import com.oneliang.ktx.frame.tester.TomcatTestCase

class TestTomcatTestCase : TomcatTestCase() {

    override fun test() {
        this.get(this.baseUrl + "/monitorReporter/deviceStatus/onlineStatus.do")
    }
}