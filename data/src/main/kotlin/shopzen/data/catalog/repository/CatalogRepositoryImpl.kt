package shopzen.data.catalog.repository

import shopzen.data.catalog.mapper.CatalogMapper.toDomain
import shopzen.data.catalog.mapper.CatalogMapper.toDistinctBrands
import shopzen.data.catalog.remote.RemoteCatalogDataSource
import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.catalog.repository.CatalogRepository
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

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = remoteDataSource.getProducts()
            val products = response.products
                ?.map { it.toDomain() }
                .orEmpty()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBrands(): Result<List<Brand>> {
        return try {
            val response = remoteDataSource.getProductVendors()
            val brands = response.products
                ?.toDistinctBrands()
                .orEmpty()
            Result.success(brands)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = remoteDataSource.getCustomCollections()
            val categories = response.customCollections
                ?.map { it.toDomain() }
                .orEmpty()
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductsByBrand(brandName: String): Result<List<Product>> {
        return try {
            val response = remoteDataSource.getProductsByVendor(brandName)
            val products = response.products
                ?.map { it.toDomain() }
                .orEmpty()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductsByCategory(categoryTitle: String): Result<List<Product>> {
        return try {
            val response = remoteDataSource.getProductsByCategory(categoryTitle)
            val products = response.products
                ?.map { it.toDomain() }
                .orEmpty()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
