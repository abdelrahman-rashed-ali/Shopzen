package shopzen.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.async
import shopzen.domain.catalog.usecase.GetCategoriesUseCase
import shopzen.domain.catalog.usecase.GetProductsUseCase
import shopzen.domain.search.model.SearchFilter
import shopzen.domain.search.model.SortOption
import shopzen.domain.search.usecase.FilterProductsUseCase
import shopzen.domain.search.usecase.SortProductsUseCase
import shopzen.presentation.search.intent.SearchIntent
import shopzen.presentation.search.state.SearchState
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val filterProductsUseCase: FilterProductsUseCase,
    private val sortProductsUseCase: SortProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        processIntent(SearchIntent.LoadInitialData)
    }

    fun processIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.LoadInitialData -> loadInitialData()
            is SearchIntent.UpdateQuery -> onQueryChanged(intent.query)
            is SearchIntent.SelectSuggestion -> onSuggestionSelected(intent.suggestion)
            is SearchIntent.SelectCategoryFilter -> {
                _state.value = _state.value.copy(selectedCategory = intent.category)
                applyFiltersAndSort()
            }
            is SearchIntent.SelectBrandFilter -> {
                _state.value = _state.value.copy(selectedBrand = intent.brand)
                applyFiltersAndSort()
            }
            is SearchIntent.SelectSortOption -> {
                _state.value = _state.value.copy(selectedSortOption = intent.sortOption)
                applyFiltersAndSort()
            }
            is SearchIntent.ClearFilters -> {
                _state.value = _state.value.copy(
                    query = "",
                    selectedCategory = null,
                    selectedBrand = null,
                    selectedSortOption = SortOption.DEFAULT,
                    hasSearched = false
                )
                applyFiltersAndSort()
            }
            is SearchIntent.ToggleFilterSheet -> {
                _state.value = _state.value.copy(
                    showFilterSheet = !_state.value.showFilterSheet
                )
            }
            is SearchIntent.ApplyFilters -> {
                _state.value = _state.value.copy(
                    selectedCategory = intent.category,
                    selectedBrand = intent.brand,
                    selectedSortOption = intent.sortOption,
                    showFilterSheet = false
                )
                applyFiltersAndSort()
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                supervisorScope {
                    val productsDeferred = async { getProductsUseCase() }
                    val categoriesDeferred = async { getCategoriesUseCase() }

                    val productsResult = productsDeferred.await()
                    val categoriesResult = categoriesDeferred.await()

                    var products = productsResult.getOrNull().orEmpty()
                    var categories = categoriesResult.getOrNull().orEmpty()

                    if (categories.isEmpty()) {
                        categories = listOf(
                            shopzen.domain.catalog.model.Category(
                                id = "fine-timepieces",
                                title = "Fine Timepieces",
                                imageUrl = "https://images.unsplash.com/photo-1587836374828-4dbafa94cf0e?q=80&w=800"
                            ),
                            shopzen.domain.catalog.model.Category(
                                id = "bracelets",
                                title = "Bracelets",
                                imageUrl = "https://images.unsplash.com/photo-1573408301185-9146fe634ad0?q=80&w=800"
                            ),
                            shopzen.domain.catalog.model.Category(
                                id = "rings",
                                title = "Rings",
                                imageUrl = "https://images.unsplash.com/photo-1605100804763-247f67b3557e?q=80&w=800"
                            ),
                            shopzen.domain.catalog.model.Category(
                                id = "necklaces",
                                title = "Necklaces",
                                imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?q=80&w=800"
                            )
                        )
                    }

                    if (products.isEmpty()) {
                        products = listOf(
                            shopzen.domain.catalog.model.Product(
                                id = "1",
                                title = "Aethelgard Diamond Ring",
                                vendor = "LUXE",
                                productType = "Fine Jewelry",
                                price = "1,200",
                                imageUrl = "https://images.unsplash.com/photo-1605100804763-247f67b3557e?q=80&w=600"
                            ),
                            shopzen.domain.catalog.model.Product(
                                id = "2",
                                title = "Obsidian Chronograph",
                                vendor = "LUXE",
                                productType = "Watches",
                                price = "4,500",
                                imageUrl = "https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3?q=80&w=600"
                            ),
                            shopzen.domain.catalog.model.Product(
                                id = "3",
                                title = "Ivory Leather Tote",
                                vendor = "LUXE",
                                productType = "Handbags",
                                price = "2,800",
                                imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?q=80&w=600"
                            ),
                            shopzen.domain.catalog.model.Product(
                                id = "4",
                                title = "Aura Pearl Hoops",
                                vendor = "LUXE",
                                productType = "Fine Jewelry",
                                price = "850",
                                imageUrl = "https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?q=80&w=600"
                            )
                        )
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        allProducts = products,
                        filteredProducts = products,
                        categories = categories
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Something went wrong. Please try again."
                )
            }
        }
    }

    private fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query, hasSearched = query.isNotBlank())
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            applyFiltersAndSort()
        }
    }

    private fun onSuggestionSelected(suggestion: String) {
        _state.value = _state.value.copy(query = suggestion, hasSearched = true)
        searchJob?.cancel()
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val currentState = _state.value
        val filter = SearchFilter(
            query = currentState.query,
            mainCategory = currentState.selectedCategory,
            brand = currentState.selectedBrand
        )
        val filtered = filterProductsUseCase(currentState.allProducts, filter)
        val sorted = sortProductsUseCase(filtered, currentState.selectedSortOption)
        _state.value = currentState.copy(filteredProducts = sorted)
    }
}
