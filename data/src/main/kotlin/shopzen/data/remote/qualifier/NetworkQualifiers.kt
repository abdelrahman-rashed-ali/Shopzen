package shopzen.data.remote.qualifier

import javax.inject.Qualifier

/**
 * Qualifies the Ktor [io.ktor.client.HttpClient] configured for the
 * Shopify **Admin** REST API (`/admin/api/{version}/`).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RestClient

/**
 * Qualifies the Ktor [io.ktor.client.HttpClient] configured for the
 * Shopify **Storefront** GraphQL API (`/api/{version}/graphql.json`).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GraphQLClient

/**
 * Qualifies the Ktor [io.ktor.client.HttpClient] configured for Paymob
 * acceptance APIs.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PaymobClient

/**
 * Qualifies the Ktor [io.ktor.client.HttpClient] configured for live exchange
 * rate lookups.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ExchangeRateClient
