package com.oneliang.ktx.frame.test.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery
import co.elastic.clients.elasticsearch.core.IndexResponse
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.oneliang.ktx.frame.elasticsearch.get
import com.oneliang.ktx.frame.elasticsearch.index
import io.github.bonigarcia.wdm.WebDriverManager
import org.apache.http.HttpHost
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.ssl.SSLContexts
import org.elasticsearch.client.RestClient
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.events.EventFiringDecorator
import org.openqa.selenium.support.events.WebDriverListener
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext


private fun indexWithHttp(esClient: ElasticsearchClient) {
    val product = Product("bk-1", "City bike", 123.0)

    val response: IndexResponse = esClient.index("products", product.sku, product)
    println("Indexed with version " + response.version())
}

private fun getWithHttp(esClient: ElasticsearchClient) {
    esClient.get("products", "bk-1", Product::class, { getResponse, product ->
        println("Product name " + product.name)
    }, { getResponse ->
        println("Product not found")
    })
}

private fun searchWithHttp(esClient: ElasticsearchClient) {
    val searchText = "bike"

    val searchResponse = esClient.search(
            { s: SearchRequest.Builder ->
                s.index("products").query { q: Query.Builder ->
                    q.match { t: MatchQuery.Builder ->
                        t.field("name").query(searchText)
                    }
                }
            },
            Product::class.java
    )

    val total = searchResponse.hits().total()
    val isExactResult = total!!.relation() == TotalHitsRelation.Eq

    if (isExactResult) {
        println("There are " + total.value() + " results")
    } else {
        println("There are more than " + total.value() + " results")
    }

    val hits = searchResponse.hits().hits()
    for (hit in hits) {
        val product = hit.source()
        println("Found product " + product!!.sku + ", score " + hit.score())
    }
}

private fun withHttp() {

    // Create the low-level client
    val restClient: RestClient = RestClient.builder(HttpHost("localhost", 9200)).build()
// Create the transport with a Jackson mapper
    val transport: ElasticsearchTransport = RestClientTransport(restClient, JacksonJsonpMapper())

// And create the API client
    val esClient = ElasticsearchClient(transport)

//    indexWithHttp(esClient)

    getWithHttp(esClient)
    searchWithHttp(esClient)
}

private fun withHttps() {
    val caPath = "D:/Dandelion/tool/elasticsearch/elasticsearch-8.4.2/config/certs/http_ca.crt"
    val caCertificatePath: Path = Paths.get(caPath)
    val factory: CertificateFactory = CertificateFactory.getInstance("X.509")
    var trustedCa: Certificate
    Files.newInputStream(caCertificatePath).use { inputStream -> trustedCa = factory.generateCertificate(inputStream) }
    val trustStore = KeyStore.getInstance("pkcs12")
    trustStore.load(null, null)
    trustStore.setCertificateEntry("ca", trustedCa)
    val sslContextBuilder: SSLContextBuilder = SSLContexts.custom()
            .loadTrustMaterial(trustStore, null)
    val sslContext: SSLContext = sslContextBuilder.build()
    val restClient: RestClient = RestClient.builder(HttpHost("localhost", 9200, "https")).setHttpClientConfigCallback { httpClientBuilder ->
        httpClientBuilder.setSSLContext(sslContext)
    }.build()

    // Create the low-level client
//    val restClient: RestClient = RestClient.builder(HttpHost("localhost", 9200)).build()
// Create the transport with a Jackson mapper
    val transport: ElasticsearchTransport = RestClientTransport(restClient, JacksonJsonpMapper())

// And create the API client
    val client = ElasticsearchClient(transport)

    val search: SearchResponse<Product> = client.search(
            { s: SearchRequest.Builder ->
                s.index("products").query { q: Query.Builder ->
                    q.term { t: TermQuery.Builder ->
                        t.field("name").value { v: FieldValue.Builder ->
                            v.stringValue("bicycle")
                        }
                    }
                }
            },
            Product::class.java
    )

    for (hit in search.hits().hits()) {
        println(hit.source())
    }
}

fun main() {
//    WebDriverManager.chromedriver().config().isUseMirror = true
    WebDriverManager.chromedriver().setup()
//    val driver: WebDriver = ChromeDriver()
    val chromeOptions = ChromeOptions()
    chromeOptions.addArguments("--headless")
    val prefs = mapOf<String, Any>(
            "permissions.default.stylesheet" to 2,
            "profile.default_content_settings.images" to 2
    )
//    chromeOptions.setExperimentalOption("permissions.default.stylesheet",2)
    chromeOptions.setExperimentalOption("prefs", prefs)
    val driver = ChromeDriver(chromeOptions)
    val listener: WebDriverListener = WebEventListener()
    val decorated: WebDriver = EventFiringDecorator<WebDriver>(listener).decorate(driver)

    decorated.get("https://www.baidu.com")
    println(decorated.title)
    decorated.quit()
//    withHttp()
}