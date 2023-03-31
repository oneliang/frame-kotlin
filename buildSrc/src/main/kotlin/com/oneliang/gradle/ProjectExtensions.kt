package com.oneliang.gradle

import Dependencies
import org.gradle.api.Project


fun Project.applyTestFeatureDependencies() {
    this.dependencies.add("implementation", Dependencies["kotlin-test"])
    this.dependencies.add("implementation", Dependencies["kotlin-test-junit"])
}

fun Project.applyFeatureDependencies() {
    this.dependencies.add("api", this.fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))
    this.dependencies.add("implementation", Dependencies["kotlin-reflect"])
    this.dependencies.add("implementation", Dependencies["kotlin-stdlib"])
    this.dependencies.add("implementation", Dependencies["kotlinx-coroutines-core"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-base"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-command"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-common"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-concurrent"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-csv"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-file"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-generate"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-http"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-jar"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-json"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-jvm"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-jxl"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-logging"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-resource"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-state"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-upload"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-math"])
    this.dependencies.add("implementation", Dependencies["util-kotlin-packet"])
}