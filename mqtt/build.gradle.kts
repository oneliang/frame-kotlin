import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(Dependencies["org-fusesource-mqtt-client"])
    implementation(project(":handler"))
}

