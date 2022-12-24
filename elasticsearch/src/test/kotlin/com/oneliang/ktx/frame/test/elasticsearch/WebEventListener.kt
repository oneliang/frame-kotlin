package com.oneliang.ktx.frame.test.elasticsearch

import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.events.WebDriverListener

class WebEventListener : WebDriverListener {
    override fun afterGetTitle(driver: WebDriver?, result: String?) {
        super.afterGetTitle(driver, result)
        println(this::afterGetTitle)
    }

    override fun beforeGetCurrentUrl(driver: WebDriver?) {
        super.beforeGetCurrentUrl(driver)
        println(this::beforeGetCurrentUrl)
    }
}