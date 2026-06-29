package iti.data.catalog.remote

import iti.data.catalog.remote.api.CatalogApiService
import iti.data.catalog.remote.dto.CollectionDto
import iti.data.catalog.remote.dto.ProductDto
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
    suspend fun getProducts(): ProductDto {
        return apiService.getProducts()
    }

    suspend fun getProductVendors(): ProductDto {
        return apiService.getProductVendors()
    }

    suspend fun getCustomCollections(): CollectionDto {
        return apiService.getCustomCollections()
    }
}
