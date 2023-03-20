import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()

tasks.processResources {
    from("src/main/kotlin") {
        include("**/*.dic")
    }
}
dependencies {
}


