import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    testImplementation(project(":expression"))
}