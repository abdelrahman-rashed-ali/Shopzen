package shopzen.data.remote.graphql

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
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import shopzen.data.remote.config.NetworkConfig

/**
 * Builds an [HttpClient] configured for the Shopify **Storefront** GraphQL API.
 *
 * Base URL  : `https://{hostname}/api/{version}/graphql.json`
 * Auth      : `X-Shopify-Storefront-Access-Token` header
 *
 * Distinct from [shopzen.data.remote.rest.KtorRestClient] which targets the Admin API.
 * Qualifies as `@GraphQLClient` in the DI graph.
 */
object KtorGraphQLClient {

    fun build(config: NetworkConfig): HttpClient = HttpClient(Android) {

        // ── Android engine ─────────────────────────────────────────────────
        engine {
            connectTimeout = config.connectTimeoutMs.toInt()
            socketTimeout  = config.readTimeoutMs.toInt()
        }

        // ── Plugin-level timeout ────────────────────────────────────────────
        install(HttpTimeout) {
            connectTimeoutMillis = config.connectTimeoutMs
            requestTimeoutMillis = config.readTimeoutMs
            socketTimeoutMillis  = config.readTimeoutMs
        }

        // ── JSON serialisation ─────────────────────────────────────────────
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

        // ── Default request — Storefront base URL + auth ────────────────────
        install(DefaultRequest) {
            val gqlUrl = config.storefrontGqlUrl.trimEnd('/')
            val withoutScheme = gqlUrl.removePrefix("https://").removePrefix("http://")
            val hostname = withoutScheme.substringBefore('/')
            val pathPart = withoutScheme.substringAfter('/', missingDelimiterValue = "")

            url {
                protocol = URLProtocol.HTTPS
                host     = hostname
                if (pathPart.isNotEmpty()) {
                    path(pathPart)
                }
            }

            config.storefrontHeaders.forEach { (key, value) ->
                header(key, value)
            }
        }

        // ── Logging (debug builds only) ────────────────────────────────────
        if (config.enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorGraphQL", message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }
}
