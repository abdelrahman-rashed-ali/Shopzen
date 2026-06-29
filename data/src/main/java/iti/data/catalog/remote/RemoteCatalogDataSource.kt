package iti.data.catalog.remote

import iti.data.catalog.remote.api.CatalogApiService
import iti.data.catalog.remote.dto.CollectionsResponse
import iti.data.catalog.remote.dto.ProductsResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for catalog operations.
 * Leverages direct API calls to keep CatalogRepositoryImpl abstract and focused on data flow orchestration.
 */
@Singleton
class RemoteCatalogDataSource @Inject constructor(
    private val apiService: CatalogApiService
) {
    suspend fun getProducts(): Response<ProductsResponse> {
        return apiService.getProducts()
    }

    suspend fun getProductVendors(): Response<ProductsResponse> {
        return apiService.getProductVendors()
    }

    suspend fun getCustomCollections(): Response<CollectionsResponse> {
        return apiService.getCustomCollections()
    }
}
