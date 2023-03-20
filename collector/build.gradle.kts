import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(project(":bean"))
    implementation(project(":cache"))
    implementation(project(":scheduler"))
    implementation(project(":task"))
}