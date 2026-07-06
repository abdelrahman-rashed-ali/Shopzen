package shopzen.presentation.brand.state

import shopzen.domain.catalog.model.Brand

data class BrandListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val brands: List<Brand> = emptyList()
)
