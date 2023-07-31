import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()

plugins {
    java
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.oneliang.ktx:gradle-ext:1.0")
    implementation("com.oneliang.ktx:util-kotlin-base:1.0")
    implementation("com.oneliang.ktx:util-kotlin-common:1.0")
    implementation("com.oneliang.ktx:util-kotlin-file:1.0")
    implementation("com.oneliang.ktx:util-kotlin-json:1.0")
    implementation("com.oneliang.ktx:util-kotlin-generate:1.0")
    implementation(project(":context"))
    implementation(project(":jdbc"))
}


