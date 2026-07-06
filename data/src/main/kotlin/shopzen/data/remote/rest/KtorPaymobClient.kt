package shopzen.data.remote.rest

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import shopzen.data.remote.config.PaymobConfig

object KtorPaymobClient {

    /** Builds a Ktor client configured for Paymob acceptance endpoints. */
    fun build(config: PaymobConfig): HttpClient = HttpClient(Android) {
        engine {
            connectTimeout = config.connectTimeoutMs.toInt()
            socketTimeout = config.readTimeoutMs.toInt()
        }

        install(HttpTimeout) {
            connectTimeoutMillis = config.connectTimeoutMs
            requestTimeoutMillis = config.readTimeoutMs
            socketTimeoutMillis = config.readTimeoutMs
        }

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = config.enableLogging
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = false
                    explicitNulls = false
                }
            )
        }

        install(DefaultRequest) {
            val base = config.baseUrl.trimEnd('/')
            val basePath = base
                .substringAfter("://")
                .substringAfter('/', missingDelimiterValue = "")

            url {
                protocol = if (base.startsWith("https")) URLProtocol.HTTPS else URLProtocol.HTTP
                host = base
                    .removePrefix("https://")
                    .removePrefix("http://")
                    .substringBefore('/')
                encodedPath = if (basePath.isBlank()) {
                    "/"
                } else {
                    "/${basePath.trim('/')}/"
                }
            }

            config.defaultHeaders.forEach { (key, value) ->
                header(key, value)
            }
        }

        if (config.enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorPaymob", message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
}
