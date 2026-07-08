package shopzen.presentation.brand.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shopzen.domain.catalog.usecase.GetProductsByBrandUseCase
import shopzen.presentation.brand.intent.BrandProductsIntent
import shopzen.presentation.brand.state.BrandProductsState
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
import javax.inject.Inject

@HiltViewModel
class BrandProductsViewModel @Inject constructor(
    private val getProductsByBrandUseCase: GetProductsByBrandUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BrandProductsState())
    val state: StateFlow<BrandProductsState> = _state.asStateFlow()

    fun processIntent(intent: BrandProductsIntent) {
        when (intent) {
            is BrandProductsIntent.LoadProducts -> loadProducts(intent.brandName)
        }
    }

    private fun loadProducts(brandName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val currency = getCurrencySymbolUseCase()
            val result = getProductsByBrandUseCase(brandName)
            result.onSuccess { products ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    products = products,
                    error = null,
                    currency = currency,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load products for brand $brandName.",
                    currency = currency
                )
            }
        }
    }
}
