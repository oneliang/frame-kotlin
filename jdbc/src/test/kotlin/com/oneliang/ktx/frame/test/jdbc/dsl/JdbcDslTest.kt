package com.oneliang.ktx.frame.test.jdbc.dsl

import com.oneliang.ktx.frame.configuration.dsl.configurationContext
import com.oneliang.ktx.frame.jdbc.dsl.annotationMappingContext
import com.oneliang.ktx.frame.jdbc.dsl.databaseContext

fun main() {
    configurationContext {
//        configuration {
//        }
        annotationMappingContext {
            it.parameters = "-PATH=/@Interceptor.txt,-T=txt,-P=com.oneliang.platform"
        }
        databaseContext {
            it.parameters = "/config/xml/database.properties"
        }
    }
}