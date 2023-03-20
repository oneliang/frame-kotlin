import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(project(":tomcat"))
    implementation(Dependencies["tomcat-embed-core"])
    implementation(Dependencies["tomcat-embed-jasper"])
}

