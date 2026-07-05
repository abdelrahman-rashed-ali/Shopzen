package shopzen.domain.catalog.repository

import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product

interface CatalogRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getBrands(): Result<List<Brand>>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getProductsByCategory(categoryTitle: String): Result<List<Product>>
}
