import com.oneliang.ktx.gradle.applyCheckKotlinCode

applyCheckKotlinCode()
dependencies {
    implementation(Dependencies["elasticsearch-java"])
    implementation(Dependencies["jackson-databind"])
    implementation(Dependencies["selenium-java"])
    implementation(Dependencies["webdrivermanager"])
    implementation(Dependencies["maven-surefire-plugin"])
    implementation(Dependencies["lucene-core"])
    implementation(Dependencies["lucene-queryparser"])
}

