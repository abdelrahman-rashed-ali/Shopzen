package shopzen.presentation.search.viewmodel

import shopzen.domain.search.model.SortOption
import shopzen.presentation.search.state.SearchState

internal fun SearchState.withCategoryFilter(category: String?): SearchState =
    copy(selectedCategory = category)

internal fun SearchState.withBrandFilter(brand: String?): SearchState =
    copy(selectedBrand = brand)

internal fun SearchState.withSortOption(sortOption: SortOption): SearchState =
    copy(selectedSortOption = sortOption)

internal fun SearchState.withClearedFilters(): SearchState =
    copy(
        query = "",
        selectedCategory = null,
        selectedBrand = null,
        selectedSortOption = SortOption.DEFAULT,
        hasSearched = false,
    )

internal fun SearchState.withToggledFilterSheet(): SearchState =
    copy(showFilterSheet = !showFilterSheet)

internal fun SearchState.withAppliedFilters(
    category: String?,
    brand: String?,
    sortOption: SortOption,
): SearchState =
    copy(
        selectedCategory = category,
        selectedBrand = brand,
        selectedSortOption = sortOption,
        showFilterSheet = false,
    )

internal fun SearchState.withQuery(query: String): SearchState =
    copy(query = query, hasSearched = query.isNotBlank())

internal fun SearchState.withSelectedSuggestion(suggestion: String): SearchState =
    copy(query = suggestion, hasSearched = true)
