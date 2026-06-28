package iti.domain.catalog.usecase

import iti.domain.catalog.model.Category
import iti.domain.catalog.repository.CatalogRepository

/**
 * Retrieves product categories (custom collections) from the catalog.
 */
class GetCategoriesUseCase(private val repository: CatalogRepository) {

    /**
     * @return [Result] wrapping the category list or an error.
     */
    suspend operator fun invoke(): Result<List<Category>> {
        return repository.getCategories()
    }
}
