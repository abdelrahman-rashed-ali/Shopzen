package shopzen.domain.search.usecase

import shopzen.domain.catalog.model.Product
import shopzen.domain.search.repository.ImageSearchRepository
import javax.inject.Inject

class SearchProductsByImageUseCase @Inject constructor(
    private val repository: ImageSearchRepository
) {
    suspend operator fun invoke(imageBytes: ByteArray): Result<List<Product>> {
        return repository.searchProductsByImage(imageBytes)
    }
}
