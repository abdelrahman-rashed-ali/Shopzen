package shopzen.presentation.search.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import shopzen.domain.catalog.usecase.GetCategoriesUseCase
import shopzen.domain.catalog.usecase.GetProductsUseCase
import shopzen.domain.search.model.SearchFilter
import shopzen.domain.search.usecase.FilterProductsUseCase
import shopzen.domain.search.usecase.SearchProductsByImageUseCase
import shopzen.domain.search.usecase.SortProductsUseCase
import shopzen.presentation.search.intent.SearchIntent
import shopzen.presentation.search.state.SearchState
import shopzen.presentation.search.util.ImageCompressor
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val filterProductsUseCase: FilterProductsUseCase,
    private val sortProductsUseCase: SortProductsUseCase,
    private val searchProductsByImageUseCase: SearchProductsByImageUseCase,
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
                _state.value = _state.value.withCategoryFilter(intent.category)
                applyFiltersAndSort()
            }
            is SearchIntent.SelectBrandFilter -> {
                _state.value = _state.value.withBrandFilter(intent.brand)
                applyFiltersAndSort()
            }
            is SearchIntent.SelectSortOption -> {
                _state.value = _state.value.withSortOption(intent.sortOption)
                applyFiltersAndSort()
            }
            is SearchIntent.ClearFilters -> {
                _state.value = _state.value.withClearedFilters()
                applyFiltersAndSort()
            }
            is SearchIntent.ToggleFilterSheet -> {
                _state.value = _state.value.withToggledFilterSheet()
            }
            is SearchIntent.ApplyFilters -> {
                _state.value = _state.value.withAppliedFilters(intent.category, intent.brand, intent.sortOption)
                applyFiltersAndSort()
            }
            is SearchIntent.SearchByImageUri -> searchByImageUri(intent.uri)
            is SearchIntent.SearchByImageBitmap -> searchByImageBitmap(intent.bitmap)
            is SearchIntent.ClearImageSearch -> {
                _state.value = _state.value.copy(
                    selectedImageUri = null,
                    query = "",
                    hasSearched = false,
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

                    val products = productsResult.getOrNull().orEmpty().ifEmpty { fallbackSearchProducts() }
                    val categories = categoriesResult.getOrNull().orEmpty().ifEmpty { fallbackSearchCategories() }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        allProducts = products,
                        filteredProducts = products,
                        categories = categories,
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
        _state.value = _state.value.withQuery(query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            applyFiltersAndSort()
        }
    }

    private fun onSuggestionSelected(suggestion: String) {
        _state.value = _state.value.withSelectedSuggestion(suggestion)
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

    private fun searchByImageUri(uri: Uri) {
        _state.value = _state.value.copy(
            isImageUploading = true,
            selectedImageUri = uri,
            error = null,
            hasSearched = true,
        )
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            val bytes = ImageCompressor.compressImageFromUri(context, uri)
            if (bytes == null) {
                _state.value = _state.value.copy(
                    isImageUploading = false,
                    error = "Failed to process image",
                )
                return@launch
            }
            performImageSearch(bytes)
        }
    }

    private fun searchByImageBitmap(bitmap: Bitmap) {
        _state.value = _state.value.copy(
            isImageUploading = true,
            selectedImageUri = null,
            error = null,
            hasSearched = true,
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
                allProducts = products,
            )
        } else {
            val e = result.exceptionOrNull()
            if (e is CancellationException) throw e
            val message = e?.message ?: "Image search failed."
            if (!message.contains("was cancelled", ignoreCase = true)) {
                _state.value = _state.value.copy(
                    isImageUploading = false,
                    error = message,
                )
            }
        }
    }
}
