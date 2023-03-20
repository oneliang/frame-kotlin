import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(Dependencies["jcraft-jsch"])
    implementation(Dependencies["jcraft-jzlib"])
    implementation(project(":ssh"))
}

