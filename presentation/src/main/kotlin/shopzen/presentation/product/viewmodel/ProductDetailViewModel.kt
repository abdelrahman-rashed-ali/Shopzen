package shopzen.presentation.product.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.product.usecase.GetProductByIdUseCase
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import shopzen.presentation.product.intent.ProductDetailIntent
import shopzen.presentation.product.state.ProductDetailState
import javax.inject.Inject

/**
 * @HiltViewModel for the Product Detail screen.
 *
 * - Reads `productId` from [SavedStateHandle]
 * - Processes [ProductDetailIntent]s via [processIntent]
 * - Observes user preferences and reactively converts prices when currency changes
 * - Exposes a single [StateFlow] of [ProductDetailState]
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle["productId"])

    private val _state = MutableStateFlow(ProductDetailState())
    val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    init {
        observeCurrency()
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

    // ── Currency observation ───────────────────────────────────────────────────

    private fun observeCurrency() {
        viewModelScope.launch {
            getUserPreferencesUseCase().collectLatest { prefs ->
                _state.update { current ->
                    current.copy(
                        currency = prefs.currency,
                        convertedPrice = convertPrice(current.rawPrice(), prefs.currency),
                        convertedCompareAtPrice = convertPrice(current.rawCompareAtPrice(), prefs.currency),
                    )
                }
            }
        }
    }

    // ── Product loading ────────────────────────────────────────────────────────

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getProductByIdUseCase(id)
                .onSuccess { product ->
                    val initialVariantId =
                        if (product.variants.size == 1) product.variants.first().id else null
                    _state.update { current ->
                        val rawPrice = product.variants
                            .find { it.id == initialVariantId }
                            ?.price ?: product.price
                        val rawCompare = product.variants
                            .find { it.id == initialVariantId }
                            ?.compareAtPrice ?: product.compareAtPrice
                        current.copy(
                            isLoading = false,
                            product = product,
                            selectedVariantId = initialVariantId,
                            error = null,
                            convertedPrice = convertPrice(rawPrice, current.currency),
                            convertedCompareAtPrice = convertPrice(rawCompare, current.currency),
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

    // ── Variant selection ──────────────────────────────────────────────────────

    private fun selectVariant(variantId: Long) {
        _state.update { current ->
            val variant = current.product?.variants?.find { it.id == variantId }
            current.copy(
                selectedVariantId = variantId,
                showSizeRequiredError = false,
                convertedPrice = convertPrice(variant?.price, current.currency),
                convertedCompareAtPrice = convertPrice(variant?.compareAtPrice, current.currency),
            )
        }
    }

    private fun addToCart() {
        // TODO: Delegate cart handling to dedicated cart feature/use case
    }

    // ── Price conversion helpers ───────────────────────────────────────────────

    private fun convertPrice(rawUsd: String?, currency: AppCurrency): Double? {
        val usd = rawUsd?.toDoubleOrNull() ?: return null
        return usd * currency.rateFromUsd
    }

    /** Returns the raw (USD) price for the currently selected variant or product default. */
    private fun ProductDetailState.rawPrice(): String? {
        val variant = product?.variants?.find { it.id == selectedVariantId }
        return variant?.price ?: product?.price
    }

    private fun ProductDetailState.rawCompareAtPrice(): String? {
        val variant = product?.variants?.find { it.id == selectedVariantId }
        return variant?.compareAtPrice ?: product?.compareAtPrice
    }
}
