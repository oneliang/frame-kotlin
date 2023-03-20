import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(Dependencies["javaee-api"])
    implementation(project(":configuration"))
    implementation(project(":context"))
    implementation(project(":bean"))
    implementation(project(":i18n"))
}