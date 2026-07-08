package shopzen.presentation.product.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.product.usecase.GetProductByIdUseCase
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import shopzen.domain.wishlist.usecase.AddToWishlistUseCase
import shopzen.domain.wishlist.usecase.GetWishlistUseCase
import shopzen.domain.wishlist.usecase.RemoveFromWishlistUseCase
import shopzen.presentation.R
import shopzen.presentation.common.util.UiText
import shopzen.presentation.product.effect.ProductDetailEffect
import shopzen.presentation.product.intent.ProductDetailIntent
import shopzen.presentation.product.state.ProductDetailState
import javax.inject.Inject

/**
 * @HiltViewModel for the Product Detail screen.
 *
 * - Reads `productId` from [SavedStateHandle] (injected from nav arguments)
 * - Processes [ProductDetailIntent]s via [processIntent]
 * - Observes user preferences and reactively converts prices when currency changes
 * - Exposes a single [StateFlow] of [ProductDetailState]
 */
@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val getWishlistUseCase: GetWishlistUseCase,
    private val addToWishlistUseCase: AddToWishlistUseCase,
    private val removeFromWishlistUseCase: RemoveFromWishlistUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle["productId"])
    private var wishlistJob: Job? = null
    private var currentUserId: String? = null

    private val _state = MutableStateFlow(ProductDetailState())
    val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    private val _effects = Channel<ProductDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observeCurrency()
        processIntent(ProductDetailIntent.LoadProduct(productId))
    }

    fun processIntent(intent: ProductDetailIntent) {
        when (intent) {
            is ProductDetailIntent.LoadProduct -> loadProduct(intent.productId)
            is ProductDetailIntent.SelectVariant -> selectVariant(intent.variantId)
            ProductDetailIntent.RequireVariantSelection -> showVariantSelectionError()
            ProductDetailIntent.ToggleWishlist -> toggleWishlist()
            is ProductDetailIntent.Retry -> loadProduct(productId)
        }
    }

    // ── Currency observation ──────────────────────────────────────────────

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

    // ── Product loading ───────────────────────────────────────────────────

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            _state.update { it.loadingProduct() }

            getProductByIdUseCase(id)
                .onSuccess { product ->
                    _state.update { current ->
                        val initialVariantId =
                            if (product.variants.size == 1) product.variants.first().id else null
                        val rawPrice = product.variants
                            .find { it.id == initialVariantId }
                            ?.price ?: product.price
                        val rawCompare = product.variants
                            .find { it.id == initialVariantId }
                            ?.compareAtPrice ?: product.compareAtPrice
                        current.withLoadedProduct(product).copy(
                            selectedVariantId = initialVariantId,
                            convertedPrice = convertPrice(rawPrice, current.currency),
                            convertedCompareAtPrice = convertPrice(rawCompare, current.currency),
                        )
                    }
                    observeWishlist(product.id)
                }
                .onFailure { throwable ->
                    _state.update { it.withProductLoadError(throwable.message) }
                }
        }
    }

    private fun observeWishlist(productId: Long) {
        wishlistJob?.cancel()
        wishlistJob = viewModelScope.launch {
            currentUserId = getCurrentUserUseCase().getOrNull()?.uid
            val userId = currentUserId
            if (userId == null) {
                _state.update { it.withWishlistItem(null) }
                return@launch
            }

            getWishlistUseCase(userId)
                .catch {
                    _state.update { state -> state.withWishlistItem(null) }
                }
                .collect { items ->
                    val matchingItem = items.findProductWishlistItem(productId)
                    _state.update { it.withWishlistItem(matchingItem) }
                }
        }
    }

    // ── Variant selection ──────────────────────────────────────────────────

    private fun selectVariant(variantId: Long) {
        _state.update { current ->
            val variant = current.product?.variants?.find { it.id == variantId }
            current.withSelectedVariant(variantId).copy(
                convertedPrice = convertPrice(variant?.price, current.currency),
                convertedCompareAtPrice = convertPrice(variant?.compareAtPrice, current.currency),
            )
        }
    }

    private fun showVariantSelectionError() {
        _state.update { it.withVariantRequiredError() }
    }

    // ── Wishlist toggling ──────────────────────────────────────────────────

    private fun toggleWishlist() {
        viewModelScope.launch {
            val product = _state.value.product ?: return@launch
            val userId = currentUserId ?: getCurrentUserUseCase().getOrNull()?.uid
            currentUserId = userId

            if (userId == null) {
                _effects.send(ProductDetailEffect.NavigateToLogin)
                return@launch
            }

            _state.update { it.withWishlistUpdating(true) }

            val wasWishlisted = _state.value.isWishlisted
            val result = _state.value.toggleWishlistResult(
                product = product,
                userId = userId,
                addToWishlist = addToWishlistUseCase::invoke,
                removeFromWishlist = removeFromWishlistUseCase::invoke,
            )

            result
                .onSuccess {
                    _state.update { it.withWishlistUpdating(false) }
                    _effects.send(
                        ProductDetailEffect.ShowSnackbar(
                            UiText.StringResource(
                                if (wasWishlisted) {
                                    R.string.product_detail_removed_from_wishlist
                                } else {
                                    R.string.product_detail_added_to_wishlist
                                },
                            ),
                        ),
                    )
                }
                .onFailure { throwable ->
                    _state.update { it.withWishlistUpdating(false) }
                    _effects.send(
                        ProductDetailEffect.ShowSnackbar(
                            throwable.message?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.product_detail_wishlist_error),
                        ),
                    )
                }
        }
    }

    // ── Price conversion helpers ───────────────────────────────────────────

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
