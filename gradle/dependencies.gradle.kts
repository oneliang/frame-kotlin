val dependencies by extra {
    mapOf(
        "junit" to "junit:junit:4.12",
        "util-kotlin-base" to "com.oneliang.ktx:util-kotlin-base:1.0",
        "util-kotlin-command" to "com.oneliang.ktx:util-kotlin-command:1.0",
        "util-kotlin-common" to "com.oneliang.ktx:util-kotlin-common:1.0",
        "util-kotlin-concurrent" to "com.oneliang.ktx:util-kotlin-concurrent:1.0",
        "util-kotlin-csv" to "com.oneliang.ktx:util-kotlin-csv:1.0",
        "util-kotlin-file" to "com.oneliang.ktx:util-kotlin-file:1.0",
        "util-kotlin-generate" to "com.oneliang.ktx:util-kotlin-generate:1.0",
        "util-kotlin-http" to "com.oneliang.ktx:util-kotlin-http:1.0",
        "util-kotlin-jar" to "com.oneliang.ktx:util-kotlin-jar:1.0",
        "util-kotlin-json" to "com.oneliang.ktx:util-kotlin-json:1.0",
        "util-kotlin-jvm" to "com.oneliang.ktx:util-kotlin-jvm:1.0",
        "util-kotlin-jxl" to "com.oneliang.ktx:util-kotlin-jxl:1.0",
        "util-kotlin-logging" to "com.oneliang.ktx:util-kotlin-logging:1.0",
        "util-kotlin-resource" to "com.oneliang.ktx:util-kotlin-resource:1.0",
        "util-kotlin-state" to "com.oneliang.ktx:util-kotlin-state:1.0",
        "util-kotlin-upload" to "com.oneliang.ktx:util-kotlin-upload:1.0",
        "util-kotlin-math" to "com.oneliang.ktx:util-kotlin-math:1.0",
        "kotlin-reflect" to "org.jetbrains.kotlin:kotlin-reflect:${Constants.kotlinVersion}",
        "kotlin-stdlib-js" to "org.jetbrains.kotlin:kotlin-stdlib-js:${Constants.kotlinVersion}",
        "javaee-api" to "javax:javaee-api:8.0",
        "kotlin-test" to "org.jetbrains.kotlin:kotlin-test",
        "kotlin-test-junit" to "org.jetbrains.kotlin:kotlin-test-junit",
        "kotlinx-coroutines-core" to "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Constants.kotlinxCoroutinesVersion}",
        "jexcelapi-jxl" to "net.sourceforge.jexcelapi:jxl:2.6.10",
        "jcraft-jsch" to "com.jcraft:jsch:0.1.54",
        "jcraft-jzlib" to "com.jcraft:jzlib:1.1.3",
        "org-fusesource-mqtt-client" to "org.fusesource.mqtt-client:mqtt-client:1.16",
        // https://mvnrepository.com/artifact/org.apache.tomcat.embed/tomcat-embed-core
        "tomcat-embed-core" to "org.apache.tomcat.embed:tomcat-embed-core:9.0.58"
    )
}