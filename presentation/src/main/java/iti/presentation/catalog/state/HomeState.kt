package iti.presentation.catalog.state

import iti.domain.catalog.model.Brand
import iti.domain.catalog.model.Category
import iti.domain.catalog.model.Product

/**
 * Immutable UI state for the Home screen.
 * Updated via copy() in the ViewModel.
 */
data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bannerImages: List<String> = emptyList(),
    val brands: List<Brand> = emptyList(),
    val categories: List<Category> = emptyList(),
    val newArrivals: List<Product> = emptyList()
)
