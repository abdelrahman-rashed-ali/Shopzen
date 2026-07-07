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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import shopzen.domain.catalog.usecase.GetCategoriesUseCase
import shopzen.domain.catalog.usecase.GetProductsUseCase
import shopzen.domain.search.model.SearchFilter
import shopzen.domain.search.model.SortOption
import shopzen.domain.search.usecase.FilterProductsUseCase
import shopzen.domain.search.usecase.SortProductsUseCase
import shopzen.domain.search.usecase.SearchProductsByImageUseCase
import shopzen.presentation.search.intent.SearchIntent
import shopzen.presentation.search.state.SearchState
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val filterProductsUseCase: FilterProductsUseCase,
    private val sortProductsUseCase: SortProductsUseCase,
    private val searchProductsByImageUseCase: SearchProductsByImageUseCase
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
            is SearchIntent.SearchByImageUri -> searchByImageUri(intent.uri)
            is SearchIntent.SearchByImageBitmap -> searchByImageBitmap(intent.bitmap)
            is SearchIntent.ClearImageSearch -> {
                _state.value = _state.value.copy(
                    selectedImageUri = null,
                    query = "",
                    hasSearched = false
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

    private fun searchByImageUri(uri: android.net.Uri) {
        _state.value = _state.value.copy(
            isImageUploading = true,
            selectedImageUri = uri,
            error = null,
            hasSearched = true
        )
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            val bytes = shopzen.presentation.search.util.ImageCompressor.compressImageFromUri(context, uri)
            if (bytes == null) {
                _state.value = _state.value.copy(
                    isImageUploading = false,
                    error = "Failed to process image"
                )
                return@launch
            }
            performImageSearch(bytes)
        }
    }

    private fun searchByImageBitmap(bitmap: android.graphics.Bitmap) {
        _state.value = _state.value.copy(
            isImageUploading = true,
            selectedImageUri = null,
            error = null,
            hasSearched = true
        )
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream)
            val bytes = stream.toByteArray()
            performImageSearch(bytes)
        }
    }

    private suspend fun performImageSearch(imageBytes: ByteArray) {
        val result = searchProductsByImageUseCase(imageBytes)
        if (result.isSuccess) {
            val products = result.getOrNull().orEmpty()
            _state.value = _state.value.copy(
                isImageUploading = false,
                filteredProducts = products,
                allProducts = products // Treat image search results as the new dataset
            )
        } else {
            val e = result.exceptionOrNull()
            if (e is CancellationException) throw e
            
            // "StandaloneCoroutine was cancelled" usually occurs when CancellationException is mistakenly displayed.
            // By filtering it out, we ensure the UI doesn't show confusing cancellation errors.
            val message = e?.message ?: "Image search failed."
            if (!message.contains("was cancelled", ignoreCase = true)) {
                _state.value = _state.value.copy(
                    isImageUploading = false,
                    error = message
                )
            }
        }
    }
}
