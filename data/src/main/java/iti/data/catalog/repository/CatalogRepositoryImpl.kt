package iti.data.catalog.repository

import iti.data.catalog.mapper.CatalogMapper.toDomain
import iti.data.catalog.mapper.CatalogMapper.toDistinctBrands
import iti.data.catalog.remote.api.CatalogApiService
import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product
import iti.domain.catalog.repository.CatalogRepository

/**
 * Concrete implementation of [CatalogRepository].
 * Fetches data from Shopify REST API via [CatalogApiService] and maps to domain models.
 */
class CatalogRepositoryImpl(
    private val apiService: CatalogApiService
) : CatalogRepository {

    /**
     * Fetches products and maps DTOs to domain [Product] list.
     */
    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getProducts()
            if (response.isSuccessful) {
                val products = response.body()?.products
                    ?.map { it.toDomain() }
                    .orEmpty()
                Result.success(products)
            } else {
                Result.failure(Exception("Failed to fetch products: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches product vendors and extracts distinct [Brand] list.
     */
    override suspend fun getBrands(): Result<List<Brand>> {
        return try {
            val response = apiService.getProductVendors()
            if (response.isSuccessful) {
                val brands = response.body()?.products
                    ?.toDistinctBrands()
                    .orEmpty()
                Result.success(brands)
            } else {
                Result.failure(Exception("Failed to fetch brands: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches custom collections and maps to domain [Category] list.
     */
    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCustomCollections()
            if (response.isSuccessful) {
                val categories = response.body()?.customCollections
                    ?.map { it.toDomain() }
                    .orEmpty()
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
