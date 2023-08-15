import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(Dependencies["jetty-webapp"])
    implementation(Dependencies["jetty-io"])
    implementation(Dependencies["jetty-server"])
    implementation(Dependencies["jetty-servlet"])
    implementation(Dependencies["jetty-util"])
}

