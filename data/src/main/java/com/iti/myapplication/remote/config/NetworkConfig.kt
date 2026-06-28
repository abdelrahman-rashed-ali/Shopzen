package com.iti.myapplication.remote.config

data class NetworkConfig(
    val hostname: String,
    val apiVersion: String,
    val apiKey: String,
    val connectTimeoutMs: Long = 15_000L,
    val readTimeoutMs: Long = 15_000L,
    val enableLogging: Boolean = false,
) {

    private val adminBase = "https://$hostname/admin/api/$apiVersion"

    val restBaseUrl: String
        get() = "$adminBase/"

    val graphqlBaseUrl: String
        get() = "$adminBase/graphql.json"

    val defaultHeaders: Map<String, String>
        get() = mapOf(
            "Accept" to "application/json",
            "Content-Type"  to "application/json",
            "X-Shopify-Access-Token" to apiKey,
        )
}