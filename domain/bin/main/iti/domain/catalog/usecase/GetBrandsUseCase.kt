package iti.domain.catalog.usecase

import iti.domain.catalog.model.Brand
import iti.domain.catalog.repository.CatalogRepository

/**
 * Retrieves distinct brands/vendors from the catalog.
 */
class GetBrandsUseCase(private val repository: CatalogRepository) {

    /**
     * @return [Result] wrapping the brand list or an error.
     */
    suspend operator fun invoke(): Result<List<Brand>> {
        return repository.getBrands()
    }
}
