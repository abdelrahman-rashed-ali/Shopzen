package shopzen.domain.search.usecase

import shopzen.domain.catalog.model.Product
import shopzen.domain.search.model.SortOption
import javax.inject.Inject

class SortProductsUseCase @Inject constructor() {
    operator fun invoke(products: List<Product>, sortOption: SortOption): List<Product> {
        return when (sortOption) {
            SortOption.DEFAULT -> products
            SortOption.PRICE_LOW_TO_HIGH -> products.sortedBy {
                it.price.replace(",", "").toDoubleOrNull() ?: 0.0
            }
            SortOption.PRICE_HIGH_TO_LOW -> products.sortedByDescending {
                it.price.replace(",", "").toDoubleOrNull() ?: 0.0
            }
            SortOption.BEST_SELLER -> products
            SortOption.BY_SUB_CATEGORY -> products.sortedBy { it.productType }
        }
    }
}
