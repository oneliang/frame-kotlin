import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(project(":broadcast"))
    implementation(project(":downloader"))
}

