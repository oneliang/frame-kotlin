package com.oneliang.ktx.frame.test.j2ee.dsl

import com.oneliang.ktx.frame.configuration.dsl.configuration
import com.oneliang.ktx.frame.configuration.dsl.configurationContext
import com.oneliang.ktx.frame.servlet.action.dsl.action
import com.oneliang.ktx.frame.servlet.action.dsl.actionContext
import com.oneliang.ktx.frame.servlet.action.dsl.annotationActionContext
import com.oneliang.ktx.frame.uri.dsl.uriMappingContext


fun main(){
    configurationContext {
        this.projectRealPath ="/Users/oneliang/Java/githubWorkspace/frame-kotlin/j2ee"
        this.classesRealPath = "/Users/oneliang/Java/githubWorkspace/frame-kotlin/j2ee/src/test/resources"
//        configuration {
//        }
        actionContext {
            it.parameter = "/config/xml/action/system-action.xml"
            action {

            }
        }
        annotationActionContext {
            it.parameter="/com/oneliang/platform"
        }
        annotationActionContext("dependencies-annotation-action") {
            it.parameter="-PATH=/@Action.txt,-T=txt,-P=com.oneliang.platform"
        }
        uriMappingContext {
            it.parameter = "/config/xml/uri/uriMapping.xml"
        }
    }
}