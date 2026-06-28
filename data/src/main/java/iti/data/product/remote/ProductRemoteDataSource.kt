package iti.data.product.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import iti.data.BuildConfig
import iti.data.product.remote.dto.ProductResponseDto

/**
 * Ktor-based remote data source for Shopify product endpoints.
 * Base URL and auth token are configured on the [HttpClient] via DI.
 */
class ProductRemoteDataSource(
    private val client: HttpClient,
) {
    /**
     * GET /admin/api/{version}/products/{id}.json
     */
    suspend fun getProductById(id: Long): ProductResponseDto {
        return client.get(
            "admin/api/${BuildConfig.SHOPIFY_API_VERSION}/products/$id.json"
        ).body()
    }
}
