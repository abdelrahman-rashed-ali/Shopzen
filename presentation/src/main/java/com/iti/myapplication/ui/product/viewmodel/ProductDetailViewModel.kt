package com.iti.myapplication.ui.product.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.myapplication.ui.product.intent.ProductDetailIntent
import com.iti.myapplication.ui.product.state.ProductDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import iti.domain.product.usecase.GetProductByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @HiltViewModel for the Product Detail screen.
 *
 * - Reads `productId` from [SavedStateHandle] (injected from nav arguments)
 * - Processes [ProductDetailIntent]s via [processIntent]
 * - Exposes a single [StateFlow] of [ProductDetailState]
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle["productId"])

    private val _state = MutableStateFlow(ProductDetailState())
    val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    init {
        processIntent(ProductDetailIntent.LoadProduct(productId))
    }

    fun processIntent(intent: ProductDetailIntent) {
        when (intent) {
            is ProductDetailIntent.LoadProduct -> loadProduct(intent.productId)
            is ProductDetailIntent.SelectVariant -> selectVariant(intent.variantId)
            is ProductDetailIntent.AddToCart -> addToCart()
            is ProductDetailIntent.Retry -> loadProduct(productId)
        }
    }

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getProductByIdUseCase(id)
                .onSuccess { product ->
                    val initialVariantId = if (product.variants.size == 1) product.variants.first().id else null
                    _state.update {
                        it.copy(
                            isLoading = false,
                            product = product,
                            selectedVariantId = initialVariantId,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Failed to load product",
                        )
                    }
                }
        }
    }

    private fun selectVariant(variantId: Long) {
        _state.update {
            it.copy(
                selectedVariantId = variantId,
                showSizeRequiredError = false,
            )
        }
    }

    private fun addToCart() {
        // TODO: Delegate cart handling to dedicated cart feature/use case
    }
}
