package com.oneliang.ktx.frame.test.tester

import com.oneliang.ktx.frame.tester.TomcatTestSuite


fun main() {
    val warFullFilename = "/Users/oneliang/Java/githubWorkspace/oneliang-team/platform/open-platform/api/monitor-reporter/build/libs/open-platform-api-monitor-reporter-1.0.war"
//    val tomcatTester = TomcatTester(warFullFilename)
//    tomcatTester.runTest()


    val tomcatTestSuite = TomcatTestSuite("aaa", warFullFilename)
    tomcatTestSuite.addTomcatTestCase(TestTomcatTestCase())
    tomcatTestSuite.runTest()
}