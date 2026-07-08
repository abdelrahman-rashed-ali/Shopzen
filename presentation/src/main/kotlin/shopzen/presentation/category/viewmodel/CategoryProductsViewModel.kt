package shopzen.presentation.category.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shopzen.domain.catalog.usecase.GetProductsByCategoryUseCase
import shopzen.presentation.category.intent.CategoryProductsIntent
import shopzen.presentation.category.state.CategoryProductsState
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
import javax.inject.Inject

/**
 * ViewModel for the Category Products screen following the MVI pattern.
 * Fetches products filtered by category title (Shopify product_type).
 */
@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CategoryProductsState())
    val state: StateFlow<CategoryProductsState> = _state.asStateFlow()

    private val categoryTitle: String = savedStateHandle.get<String>("categoryTitle").orEmpty()

    init {
        if (categoryTitle.isNotEmpty()) {
            processIntent(CategoryProductsIntent.LoadProducts(categoryTitle))
        }
    }

    fun processIntent(intent: CategoryProductsIntent) {
        when (intent) {
            is CategoryProductsIntent.LoadProducts -> loadProducts(intent.categoryTitle)
            is CategoryProductsIntent.Retry -> loadProducts(_state.value.categoryTitle)
        }
    }

    private fun loadProducts(categoryTitle: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                categoryTitle = categoryTitle
            )

            val currency = getCurrencySymbolUseCase()
            val result = getProductsByCategoryUseCase(categoryTitle)

            result.fold(
                onSuccess = { products ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        products = products,
                        currency = currency
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load products",
                        currency = currency
                    )
                }
            )
        }
    }
}
