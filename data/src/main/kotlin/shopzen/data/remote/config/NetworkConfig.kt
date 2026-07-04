package shopzen.data.remote.config

import android.util.Base64

data class NetworkConfig(
    val hostname: String,
    val apiVersion: String,
    val apiKey: String,
    val apiPassword: String,
    val storefrontToken: String,
    val connectTimeoutMs: Long = 15_000L,
    val readTimeoutMs: Long = 15_000L,
    val enableLogging: Boolean = false,
) {

    private val adminBase       = "https://$hostname/admin/api/$apiVersion"
    private val storefrontBase  = "https://$hostname/api/$apiVersion"

    val restBaseUrl: String
        get() = "$adminBase/"

    val graphqlBaseUrl: String
        get() = "$adminBase/graphql.json"

    val storefrontGqlUrl: String
        get() = "$storefrontBase/graphql.json"

    val basicAuthHeader: String
        get() {
            val credentials = "$apiKey:$apiPassword"
            val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            return "Basic $encoded"
        }

    val defaultHeaders: Map<String, String>
        get() = mapOf(
            "Accept"                    to "application/json",
            "Content-Type"              to "application/json",
            "Authorization"             to basicAuthHeader,
            "X-Shopify-Access-Token"    to apiKey,
        )

    val storefrontHeaders: Map<String, String>
        get() = mapOf(
            "Accept"                                to "application/json",
            "Content-Type"                          to "application/json",
            "X-Shopify-Storefront-Access-Token"     to storefrontToken,
        )
}

