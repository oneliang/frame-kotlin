object Dependencies {

    private val dependencies = mapOf(
        "junit" to "junit:junit:4.12",
        "kotlin-reflect" to "org.jetbrains.kotlin:kotlin-reflect:${Constants.kotlinVersion}",
        "kotlin-stdlib" to "org.jetbrains.kotlin:kotlin-stdlib:${Constants.kotlinVersion}",
        "kotlin-stdlib-js" to "org.jetbrains.kotlin:kotlin-stdlib-js:${Constants.kotlinVersion}",
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
        "util-kotlin-section" to "com.oneliang.ktx:util-kotlin-section:1.0",
        "util-kotlin-packet" to "com.oneliang.ktx:util-kotlin-packet:1.0",
        "javaee-api" to "javax:javaee-api:8.0",
        //kotlin-test
        "kotlin-test" to "org.jetbrains.kotlin:kotlin-test",
        "kotlin-test-junit" to "org.jetbrains.kotlin:kotlin-test-junit",
        //kotlin-coroutines
        "kotlinx-coroutines-core" to "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Constants.kotlinxCoroutinesVersion}",
        //jxl about excel
        "jexcelapi-jxl" to "net.sourceforge.jexcelapi:jxl:2.6.10",
        //jcraft about ssh
        "jcraft-jsch" to "com.jcraft:jsch:0.1.54",
        "jcraft-jzlib" to "com.jcraft:jzlib:1.1.3",
        "org-fusesource-mqtt-client" to "org.fusesource.mqtt-client:mqtt-client:1.16",
        //tomcat-embed
        "tomcat-embed-core" to "org.apache.tomcat.embed:tomcat-embed-core:9.0.58",
        "tomcat-embed-jasper" to "org.apache.tomcat.embed:tomcat-embed-jasper:9.0.58",
        //netty asynchronize event-driven network application framework
        "netty-all" to "io.netty:netty-all:4.1.47.Final",
        //mongo
        "mongodb-driver-sync" to "org.mongodb:mongodb-driver-sync:4.7.0",
        //elastic search
        "elasticsearch-java" to "co.elastic.clients:elasticsearch-java:8.4.3",
        "jackson-databind" to "com.fasterxml.jackson.core:jackson-databind:2.12.3",
        //selenium
        "selenium-java" to "org.seleniumhq.selenium:selenium-java:4.7.0",
        //webdrivermanager
        "webdrivermanager" to "io.github.bonigarcia:webdrivermanager:5.2.3",
        //maven plugins
        "maven-surefire-plugin" to "org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7",
        //lucene
        "lucene-core" to "org.apache.lucene:lucene-core:9.4.2",
        "lucene-queryparser" to "org.apache.lucene:lucene-queryparser:9.4.2",
        //zookeeper
        "zookeeper" to "org.apache.zookeeper:zookeeper:3.9.0",
        //jetty
        "jetty-webapp" to "org.eclipse.jetty:jetty-webapp:9.4.51.v20230217",
        "jetty-io" to "org.eclipse.jetty:jetty-io:9.4.51.v20230217",
        "jetty-server" to "org.eclipse.jetty:jetty-server:9.4.51.v20230217",
        "jetty-servlet" to "org.eclipse.jetty:jetty-servlet:9.4.51.v20230217",
        "jetty-util" to "org.eclipse.jetty:jetty-util:9.4.51.v20230217"
    )

    operator fun get(key: String): String {
        return dependencies[key].toString()
    }
}

