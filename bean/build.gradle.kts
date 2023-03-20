import com.oneliang.ktx.gradle.applyCheckKotlinCode

apply(plugin = "kotlinx-serialization")
applyCheckKotlinCode()

dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}