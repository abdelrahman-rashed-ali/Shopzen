package shopzen.presentation.search.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
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
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.catalog.usecase.GetCategoriesUseCase
import shopzen.domain.catalog.usecase.GetProductsUseCase
import shopzen.domain.search.model.SearchFilter
import shopzen.domain.search.usecase.FilterProductsUseCase
import shopzen.domain.search.usecase.SearchProductsByImageUseCase
import shopzen.domain.search.usecase.SortProductsUseCase
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
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
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    /**
     * Keeps the original catalog dataset available after image search temporarily
     * replaces [SearchState.allProducts] with image-search results.
     */
    private var catalogProducts: List<Product> = emptyList()

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
                _state.value = _state.value.withAppliedFilters(
                    category = intent.category,
                    brand = intent.brand,
                    sortOption = intent.sortOption,
                )
                applyFiltersAndSort()
            }

            is SearchIntent.SearchByImageUri -> searchByImageUri(intent.uri)
            is SearchIntent.SearchByImageBitmap -> searchByImageBitmap(intent.bitmap)
            is SearchIntent.ClearImageSearch -> clearImageSearch()
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
                    val products = productsResult
                        .getOrNull()
                        .orEmpty()
                        .ifEmpty { fallbackSearchProducts() }

                    val categories = categoriesResult
                        .getOrNull()
                        .orEmpty()
                        .ifEmpty { fallbackSearchCategories() }

                    catalogProducts = products

                    val currency = getCurrencySymbolUseCase()

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        allProducts = products,
                        filteredProducts = products,
                        categories = categories,
                        currency = currency,
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Something went wrong. Please try again.",
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
            brand = currentState.selectedBrand,
        )

        val filtered = filterProductsUseCase(currentState.allProducts, filter)
        val sorted = sortProductsUseCase(filtered, currentState.selectedSortOption)

        _state.value = currentState.copy(filteredProducts = sorted)
    }

    private fun searchByImageUri(uri: Uri) {
        searchJob?.cancel()

        _state.value = _state.value.copy(
            isImageUploading = true,
            selectedImageUri = uri,
            error = null,
            hasSearched = true,
        )

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = ImageCompressor.compressImageFromUri(context, uri)

                if (bytes == null) {
                    _state.value = _state.value.copy(
                        isImageUploading = false,
                        error = "Failed to process image.",
                    )
                    return@launch
                }

                performImageSearch(imageBytes = bytes)
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                _state.value = _state.value.copy(
                    isImageUploading = false,
                    error = e.message ?: "Failed to process image.",
                )
            }
        }
    }

    private fun searchByImageBitmap(bitmap: Bitmap) {
        searchJob?.cancel()

        _state.value = _state.value.copy(
            isImageUploading = true,
            selectedImageUri = null,
            error = null,
            hasSearched = true,
        )

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_SEARCH_JPEG_QUALITY, stream)
                val bytes = stream.toByteArray()

                performImageSearch(imageBytes = bytes)
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                _state.value = _state.value.copy(
                    isImageUploading = false,
                    error = e.message ?: "Failed to process image.",
                )
            }
        }
    }

    private suspend fun performImageSearch(imageBytes: ByteArray) {
        val result = searchProductsByImageUseCase(imageBytes)

        result
            .onSuccess { products ->
                _state.value = _state.value.copy(
                    isImageUploading = false,
                    error = null,
                    allProducts = products,
                    filteredProducts = products,
                    hasSearched = true,
                )
            }
            .onFailure { throwable ->
                if (throwable is CancellationException) throw throwable

                val message = throwable.message ?: "Image search failed."

                // Avoid surfacing noisy coroutine cancellation text to the user.
                if (!message.contains("was cancelled", ignoreCase = true)) {
                    _state.value = _state.value.copy(
                        isImageUploading = false,
                        error = message,
                    )
                } else {
                    _state.value = _state.value.copy(isImageUploading = false)
                }
            }
    }

    private fun clearImageSearch() {
        searchJob?.cancel()

        val restoredProducts = catalogProducts.ifEmpty { _state.value.allProducts }

        _state.value = _state.value.copy(
            selectedImageUri = null,
            isImageUploading = false,
            error = null,
            query = "",
            hasSearched = false,
            allProducts = restoredProducts,
            filteredProducts = restoredProducts,
        )

        applyFiltersAndSort()
    }

    companion object {
        private const val IMAGE_SEARCH_JPEG_QUALITY = 90
    }
}
