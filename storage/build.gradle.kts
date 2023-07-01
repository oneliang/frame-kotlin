import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(Dependencies["util-kotlin-section"])
    implementation(project(":tokenization"))
    implementation(project(":feature"))
}