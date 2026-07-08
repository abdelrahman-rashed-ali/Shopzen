package shopzen.presentation.catalog.state

import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bannerImages: List<String> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val categories: List<Category> = emptyList(),
    val newArrivals: List<Product> = emptyList()
)
