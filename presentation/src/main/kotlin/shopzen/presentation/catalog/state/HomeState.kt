package shopzen.presentation.catalog.state

import shopzen.domain.ads.model.Ad
import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.profile.model.AppCurrency

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bannerImages: List<String> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val categories: List<Category> = emptyList(),
    val newArrivals: List<Product> = emptyList(),
    val ads: List<Ad> = emptyList(),
    val currency: AppCurrency = AppCurrency.USD,
)
