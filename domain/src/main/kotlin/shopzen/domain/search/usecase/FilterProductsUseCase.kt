package shopzen.domain.search.usecase

import shopzen.domain.catalog.model.Product
import shopzen.domain.search.model.SearchFilter
import javax.inject.Inject

class FilterProductsUseCase @Inject constructor() {
    operator fun invoke(products: List<Product>, filter: SearchFilter): List<Product> {
        return products.filter { product ->
            val matchesQuery = filter.query.isBlank() ||
                    product.title.contains(filter.query, ignoreCase = true) ||
                    product.vendor.contains(filter.query, ignoreCase = true) ||
                    product.productType.contains(filter.query, ignoreCase = true)

            val matchesCategory = filter.mainCategory == null ||
                    product.productType.contains(filter.mainCategory, ignoreCase = true) ||
                    product.title.contains(filter.mainCategory, ignoreCase = true) ||
                    (filter.mainCategory.contains("timepieces", ignoreCase = true) && product.productType.equals("Watches", ignoreCase = true)) ||
                    (filter.mainCategory.contains("rings", ignoreCase = true) && product.title.contains("Ring", ignoreCase = true)) ||
                    (filter.mainCategory.contains("bracelets", ignoreCase = true) && product.title.contains("Bracelet", ignoreCase = true)) ||
                    (filter.mainCategory.contains("necklaces", ignoreCase = true) && product.title.contains("Necklace", ignoreCase = true))

            val matchesBrand = filter.brand == null ||
                    product.vendor.equals(filter.brand, ignoreCase = true)

            matchesQuery && matchesCategory && matchesBrand
        }
    }
}
