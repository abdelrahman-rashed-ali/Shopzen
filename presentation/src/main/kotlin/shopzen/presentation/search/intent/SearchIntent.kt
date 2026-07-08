package shopzen.presentation.search.intent

import shopzen.domain.search.model.SortOption

sealed class SearchIntent {
    object LoadInitialData : SearchIntent()
    data class UpdateQuery(val query: String) : SearchIntent()
    data class SelectSuggestion(val suggestion: String) : SearchIntent()
    data class SelectCategoryFilter(val category: String?) : SearchIntent()
    data class SelectBrandFilter(val brand: String?) : SearchIntent()
    data class SelectSortOption(val sortOption: SortOption) : SearchIntent()
    object ClearFilters : SearchIntent()
    object ToggleFilterSheet : SearchIntent()
    data class ApplyFilters(
        val category: String?,
        val brand: String?,
        val sortOption: SortOption
    ) : SearchIntent()
}
