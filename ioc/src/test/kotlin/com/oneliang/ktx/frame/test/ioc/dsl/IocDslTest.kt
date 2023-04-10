package com.oneliang.ktx.frame.test.ioc.dsl

import com.oneliang.ktx.frame.configuration.dsl.configuration
import com.oneliang.ktx.frame.configuration.dsl.configurationContext
import com.oneliang.ktx.frame.ioc.dsl.annotationIocContext
import com.oneliang.ktx.frame.ioc.dsl.iocContext


fun main() {
    configurationContext {
//        configuration {
//        }
        iocContext {
            it.parameter = "/config/xml/ioc/system-ioc.xml"
        }
        annotationIocContext {
            it.parameter = "-PATH=/@Interceptor.txt,-T=txt,-P=com.oneliang.platform"
        }

    }
}