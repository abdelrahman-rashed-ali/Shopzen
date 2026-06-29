package iti.domain.product.usecase

import iti.domain.product.model.Product
import iti.domain.product.repository.ProductRepository
import javax.inject.Inject

/**
 * Fetches a single product by its Shopify ID.
 * One class, one public operator fun invoke — per AGENTS.md convention.
 */
class GetProductByIdUseCase @Inject constructor(
    private val repository: ProductRepository,
) {
    suspend operator fun invoke(productId: Long): Result<Product> {
        return repository.getProductById(productId)
    }
}
