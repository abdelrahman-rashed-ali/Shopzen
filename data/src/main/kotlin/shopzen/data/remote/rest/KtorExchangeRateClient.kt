package shopzen.data.remote.rest

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorExchangeRateClient {

    /** Builds a Ktor client for ExchangeRate-API open access endpoints. */
    fun build(): HttpClient = HttpClient(Android) {
        engine {
            connectTimeout = TIMEOUT_MS.toInt()
            socketTimeout = TIMEOUT_MS.toInt()
        }

        install(HttpTimeout) {
            connectTimeoutMillis = TIMEOUT_MS
            requestTimeoutMillis = TIMEOUT_MS
            socketTimeoutMillis = TIMEOUT_MS
        }

        install(ContentNegotiation) {
            json(
                Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }

        install(DefaultRequest) {
            url {
                protocol = URLProtocol.HTTPS
                host = HOST
                encodedPath = "/"
            }
            header("Accept", "application/json")
        }
    }

    private const val HOST = "open.er-api.com"
    private const val TIMEOUT_MS = 15_000L
}
