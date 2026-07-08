package shopzen.presentation.brand.state

import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.catalog.model.Product

data class BrandProductsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val brandName: String = "",
    val currency: AppCurrency = AppCurrency.USD,
)
