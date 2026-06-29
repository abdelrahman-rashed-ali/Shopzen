package iti.data.catalog.repository

import iti.data.catalog.mapper.CatalogMapper.toDomain
import iti.data.catalog.mapper.CatalogMapper.toDistinctBrands
import iti.data.catalog.remote.RemoteCatalogDataSource
import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product
import iti.domain.catalog.repository.CatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [CatalogRepository].
 * Orchestrates local/remote data flow and maps DTOs to pure domain models.
 */
@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteCatalogDataSource
) : CatalogRepository {

    /**
     * Fetches products from remote data source and maps DTOs to domain [Product] list.
     */
    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = remoteDataSource.getProducts()
            if (response.isSuccessful) {
                val products = response.body()?.products
                    ?.map { it.toDomain() }
                    .orEmpty()
                Result.success(products)
            } else {
                Result.failure(Exception("Failed to fetch products: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches product vendors from remote data source and extracts distinct [Brand] list.
     */
    override suspend fun getBrands(): Result<List<Brand>> {
        return try {
            val response = remoteDataSource.getProductVendors()
            if (response.isSuccessful) {
                val brands = response.body()?.products
                    ?.toDistinctBrands()
                    .orEmpty()
                Result.success(brands)
            } else {
                Result.failure(Exception("Failed to fetch brands: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches custom collections from remote data source and maps to domain [Category] list.
     */
    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = remoteDataSource.getCustomCollections()
            if (response.isSuccessful) {
                val categories = response.body()?.customCollections
                    ?.map { it.toDomain() }
                    .orEmpty()
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
