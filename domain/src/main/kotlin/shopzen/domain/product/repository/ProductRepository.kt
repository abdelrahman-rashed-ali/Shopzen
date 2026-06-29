package shopzen.domain.product.repository

import shopzen.domain.product.model.Product

/**
 * Repository contract for product data access.
 * Implemented in :data module — :domain only knows the interface.
 */
interface ProductRepository {
    suspend fun getProductById(id: Long): Result<Product>
}
