package shopzen.data.remote.rest

import android.util.Log
import shopzen.data.remote.config.NetworkConfig
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

object KtorRestClient {

    fun build(config: NetworkConfig): HttpClient = HttpClient(Android) {

        // ── Android engine ────────────────────────────────────────────────────
        engine {
            connectTimeout = config.connectTimeoutMs.toInt()
            socketTimeout  = config.readTimeoutMs.toInt()
        }

        // ── Plugin-level timeout (per-request override support) ───────────────
        install(HttpTimeout) {
            connectTimeoutMillis = config.connectTimeoutMs
            requestTimeoutMillis = config.readTimeoutMs
            socketTimeoutMillis  = config.readTimeoutMs
        }

        // ── JSON serialisation ────────────────────────────────────────────────
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint       = config.enableLogging
                    isLenient         = true
                    ignoreUnknownKeys = true
                    encodeDefaults    = false
                    explicitNulls     = false
                }
            )
        }

        // ── Default request — base URL + Shopify auth + shared headers ────────
        install(DefaultRequest) {
            val base = config.restBaseUrl.trimEnd('/')

            url {
                protocol = if (base.startsWith("https")) URLProtocol.HTTPS else URLProtocol.HTTP

                host = base
                    .removePrefix("https://")
                    .removePrefix("http://")
                    .substringBefore('/')

                encodedPath = "/" + base
                    .substringAfter("://")
                    .substringAfter('/', missingDelimiterValue = "") + "/"
            }

            config.defaultHeaders.forEach { (key, value) ->
                header(key, value)
            }
        }

        // ── Logging (debug builds only) ───────────────────────────────────────
        if (config.enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorREST", message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
}
