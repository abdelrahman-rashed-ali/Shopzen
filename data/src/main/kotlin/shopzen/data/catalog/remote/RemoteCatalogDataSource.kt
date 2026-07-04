package shopzen.data.catalog.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import shopzen.data.catalog.remote.dto.CollectionsResponse
import shopzen.data.catalog.remote.dto.ProductsResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ktor-based remote data source for Shopify catalog endpoints.
 * Base URL, headers, and serialisation are pre-configured on [HttpClient] by the DI module.
 */
@Singleton
class RemoteCatalogDataSource @Inject constructor(
    private val client: HttpClient
) {
    suspend fun getProducts(): ProductsResponse {
        return client.get("products.json") {
            parameter("limit", 50)
        }.body()
    }

    suspend fun getProductVendors(): ProductsResponse {
        return client.get("products.json") {
            parameter("fields", "id,vendor,images")
        }.body()
    }

    suspend fun getProductsByVendor(vendorName: String): ProductsResponse {
        return client.get("products.json") {
            parameter("vendor", vendorName)
        }.body()
    }

    suspend fun getCustomCollections(): CollectionsResponse {
        return client.get("custom_collections.json").body()
    }
}
