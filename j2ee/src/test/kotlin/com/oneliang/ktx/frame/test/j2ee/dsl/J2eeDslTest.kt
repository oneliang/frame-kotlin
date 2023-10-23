package com.oneliang.ktx.frame.test.j2ee.dsl

import com.oneliang.ktx.frame.configuration.dsl.configurationContext
import com.oneliang.ktx.frame.servlet.action.dsl.action
import com.oneliang.ktx.frame.servlet.action.dsl.actionContext
import com.oneliang.ktx.frame.servlet.action.dsl.annotationActionContext
import com.oneliang.ktx.frame.uri.dsl.uriMappingContext


fun main() {
    configurationContext {
        this.projectRealPath = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/j2ee"
        this.classesRealPath = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/j2ee/src/test/resources"
//        configuration {
//        }
        actionContext {
            it.parameters = "/config/xml/action/system-action.xml"
            action {

            }
        }
        annotationActionContext {
            it.parameters = "/com/oneliang/platform"
        }
        annotationActionContext("dependencies-annotation-action") {
            it.parameters = "-PATH=/@Action.txt,-T=txt,-P=com.oneliang.platform"
        }
        uriMappingContext {
            it.parameters = "/config/xml/uri/uriMapping.xml"
        }
    }
}