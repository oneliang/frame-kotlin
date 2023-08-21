import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(project(":tomcat"))
    implementation(project(":jetty"))
}

