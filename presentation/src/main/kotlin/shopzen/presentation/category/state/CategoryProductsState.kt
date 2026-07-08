package shopzen.presentation.category.state

import shopzen.domain.catalog.model.Product

data class CategoryProductsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categoryTitle: String = "",
    val products: List<Product> = emptyList()
)
