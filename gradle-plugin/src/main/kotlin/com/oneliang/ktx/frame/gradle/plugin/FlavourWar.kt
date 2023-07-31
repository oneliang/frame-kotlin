package com.oneliang.ktx.frame.gradle.plugin

import com.oneliang.ktx.Constants
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import javax.inject.Inject

open class FlavourWar @Inject constructor(val name: String) {

    companion object {
        const val NAME = "flavourWar"
    }

    //    private var name = name
    var from = Constants.String.BLANK
    var to = Constants.String.BLANK
    var replaceItems = emptyMap<String, String>()
    var dependsOnTaskName = Constants.String.BLANK
    var deleteFrom = false
    var generate: () -> Unit = {}
}

fun Project.flavourWar(configuration: Action<NamedDomainObjectContainer<FlavourWar>>) {
    project.extensions.configure(FlavourWar.NAME, configuration)
}