package shopzen.presentation.search.state

import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.search.model.SortOption

data class SearchState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val suggestions: List<String> = listOf("Gold Bracelets", "Swiss Watches", "Diamond Rings"),
    val selectedCategory: String? = null,
    val selectedBrand: String? = null,
    val selectedSortOption: SortOption = SortOption.DEFAULT,
    val showFilterSheet: Boolean = false,
    val hasSearched: Boolean = false
)
