package iti.domain.catalog.usecase

import iti.domain.catalog.model.Product
import iti.domain.catalog.repository.CatalogRepository

/**
 * Retrieves all products from the catalog.
 */
class GetProductsUseCase(private val repository: CatalogRepository) {

    /**
     * @return [Result] wrapping the product list or an error.
     */
    suspend operator fun invoke(): Result<List<Product>> {
        return repository.getProducts()
    }
}
