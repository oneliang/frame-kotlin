import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(project(":cache"))
    implementation(project(":coroutine"))
}