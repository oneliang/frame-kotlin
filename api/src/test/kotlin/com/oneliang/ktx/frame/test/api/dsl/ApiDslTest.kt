package com.oneliang.ktx.frame.test.api.dsl

import com.oneliang.ktx.frame.api.dsl.annotationApiContext
import com.oneliang.ktx.frame.configuration.dsl.configurationContext

fun main() {
    configurationContext {
        annotationApiContext {
            it.parameter = "/com/oneliang/platform/internal/api"
        }
    }
}