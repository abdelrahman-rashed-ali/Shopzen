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

object KtorImageSearchClient {

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
                protocol = URLProtocol.HTTP
                host = HOST
                encodedPath = "/api/"
            }
            header("Accept", "application/json")
        }
    }

    private const val HOST = "40.89.162.69"
    private const val TIMEOUT_MS = 60_000L // 60 seconds, as image upload might take time
}
