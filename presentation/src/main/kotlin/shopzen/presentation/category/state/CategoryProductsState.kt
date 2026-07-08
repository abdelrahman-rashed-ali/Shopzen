package shopzen.presentation.category.state

import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.catalog.model.Product

data class CategoryProductsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val categoryTitle: String = "",
    val currency: AppCurrency = AppCurrency.USD,
)
