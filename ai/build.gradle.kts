import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(project(":coroutine"))
    implementation("com.aparapi:aparapi:2.0.0")
}

