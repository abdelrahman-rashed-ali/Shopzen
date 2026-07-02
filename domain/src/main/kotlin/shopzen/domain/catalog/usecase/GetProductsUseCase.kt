package shopzen.domain.catalog.usecase

import shopzen.domain.catalog.model.Product
import shopzen.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): Result<List<Product>> {
        return repository.getProducts()
    }
}
