package shopzen.domain.search.repository

import shopzen.domain.catalog.model.Product

interface ImageSearchRepository {
    suspend fun searchProductsByImage(imageBytes: ByteArray): Result<List<Product>>
}
