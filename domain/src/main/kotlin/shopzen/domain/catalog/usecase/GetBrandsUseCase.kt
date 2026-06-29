package shopzen.domain.catalog.usecase

import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class GetBrandsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): Result<List<Brand>> {
        return repository.getBrands()
    }
}
