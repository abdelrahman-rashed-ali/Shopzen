package shopzen.domain.catalog.usecase

import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return repository.getCategories()
    }
}
