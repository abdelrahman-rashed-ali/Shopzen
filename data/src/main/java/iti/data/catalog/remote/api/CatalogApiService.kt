package iti.data.catalog.remote.api

import iti.data.catalog.remote.dto.CollectionsResponse
import iti.data.catalog.remote.dto.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Shopify REST Admin API catalog endpoints.
 */
interface CatalogApiService {

    @GET("products.json")
    suspend fun getProducts(
        @Query("limit") limit: Int = 50
    ): Response<ProductsResponse>

    /**
      * Fetches products with only the vendor field for brand extraction.
      * @param fields Comma-separated list of fields to include.
      */
    @GET("products.json")
    suspend fun getProductVendors(
        @Query("fields") fields: String = "id,vendor,image"
    ): Response<ProductsResponse>

    /**
      * Fetches custom collections as categories.
      */
    @GET("custom_collections.json")
    suspend fun getCustomCollections(): Response<CollectionsResponse>
}
