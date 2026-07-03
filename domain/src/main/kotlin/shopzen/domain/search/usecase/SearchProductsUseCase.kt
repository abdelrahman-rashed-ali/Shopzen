package shopzen.domain.search.usecase

import shopzen.domain.catalog.model.Product
import shopzen.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(query: String): Result<List<Product>> {
        return try {
            val result = repository.getProducts()
            if (result.isSuccess) {
                val allProducts = result.getOrNull().orEmpty()
                val filtered = if (query.isBlank()) {
                    allProducts
                } else {
                    allProducts.filter { product ->
                        product.title.contains(query, ignoreCase = true) ||
                                product.vendor.contains(query, ignoreCase = true) ||
                                product.productType.contains(query, ignoreCase = true)
                    }
                }
                Result.success(filtered)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
