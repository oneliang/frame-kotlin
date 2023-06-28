import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(project(":storage"))
    implementation(project(":tokenization"))
    implementation(project(":cache"))
    implementation(project(":feature"))
}

