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
import shopzen.domain.product.model.Product
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
 * - Reads `productId` from [SavedStateHandle].
 * - Processes [ProductDetailIntent]s through [processIntent].
 * - Observes user preferences and reactively converts prices when currency changes.
 * - Observes wishlist state for the loaded product.
 * - Exposes a single [StateFlow] of [ProductDetailState].
 * - Emits one-shot UI events through [effects].
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

    // ── Currency observation ───────────────────────────────────────────────────

    private fun observeCurrency() {
        viewModelScope.launch {
            getUserPreferencesUseCase().collectLatest { prefs ->
                _state.update { current ->
                    current.copy(
                        currency = prefs.currency,
                        convertedPrice = convertPrice(current.rawPrice(), prefs.currency),
                        convertedCompareAtPrice = convertPrice(
                            rawUsd = current.rawCompareAtPrice(),
                            currency = prefs.currency,
                        ),
                    )
                }
            }
        }
    }

    // ── Product loading ────────────────────────────────────────────────────────

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            _state.update { it.loadingProduct() }

            getProductByIdUseCase(id)
                .onSuccess { product ->
                    _state.update { current ->
                        current.withLoadedProduct(product)
                    }
                    observeWishlist(product.id)
                }
                .onFailure { throwable ->
                    _state.update { it.withProductLoadError(throwable.message) }
                }
        }
    }

    // ── Wishlist observation ───────────────────────────────────────────────────

    private fun observeWishlist(productId: Long) {
        wishlistJob?.cancel()
        wishlistJob = viewModelScope.launch {
            currentUserId = getCurrentUserUseCase().getOrNull()?.uid
            val userId = currentUserId

            if (userId == null) {
                _state.update { it.withWishlistItem(wishlistItemId = null) }
                return@launch
            }

            getWishlistUseCase(userId)
                .catch {
                    _state.update { state ->
                        state.withWishlistItem(wishlistItemId = null)
                    }
                }
                .collect { items ->
                    val matchingItem = items.find {
                        it.productId.toString() == productId.toString()
                    }

                    _state.update {
                        it.withWishlistItem(
                            wishlistItemId = matchingItem?.id?.toString(),
                        )
                    }
                }
        }
    }

    // ── Variant selection ──────────────────────────────────────────────────────

    private fun selectVariant(variantId: Long) {
        _state.update { current ->
            current.withSelectedVariant(variantId)
        }
    }

    private fun showVariantSelectionError() {
        _state.update { it.withVariantRequiredError() }
    }

    // ── Wishlist mutation ──────────────────────────────────────────────────────

    private fun toggleWishlist() {
        viewModelScope.launch {
            val product = _state.value.product ?: return@launch
            val userId = currentUserId ?: getCurrentUserUseCase().getOrNull()?.uid
            currentUserId = userId

            if (userId == null) {
                _effects.send(ProductDetailEffect.NavigateToLogin)
                return@launch
            }

            val wasWishlisted = _state.value.isWishlisted
            val wishlistItemId = _state.value.wishlistItemId

            _state.update { it.withWishlistUpdating(isUpdating = true) }

            val result = if (wasWishlisted) {
                if (wishlistItemId == null) {
                    Result.failure(IllegalStateException("Wishlist item was not found."))
                } else {
                    removeFromWishlistUseCase(wishlistItemId)
                }
            } else {
                addToWishlistUseCase(
                    product.toWishlistItem(userId, _state.value.selectedVariantId)
                )
            }

            result
                .onSuccess {
                    _state.update { it.withWishlistUpdating(isUpdating = false) }
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
                    _state.update { it.withWishlistUpdating(isUpdating = false) }
                    _effects.send(
                        ProductDetailEffect.ShowSnackbar(
                            throwable.message?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.product_detail_wishlist_error),
                        ),
                    )
                }
        }
    }

    private fun addToCart() {
        // TODO: Delegate cart handling to dedicated cart feature/use case.
    }

    // ── State reducers ─────────────────────────────────────────────────────────

    private fun ProductDetailState.loadingProduct(): ProductDetailState =
        copy(
            isLoading = true,
            error = null,
        )

    private fun ProductDetailState.withLoadedProduct(product: Product): ProductDetailState {
        val initialVariantId = product.variants
            .singleOrNull()
            ?.id

        val rawPrice = product.variants
            .find { it.id == initialVariantId }
            ?.price ?: product.price

        val rawCompareAtPrice = product.variants
            .find { it.id == initialVariantId }
            ?.compareAtPrice ?: product.compareAtPrice

        return copy(
            isLoading = false,
            error = null,
            product = product,
            selectedVariantId = initialVariantId,
            showVariantRequiredError = false,
            showSizeRequiredError = false,
            convertedPrice = convertPrice(rawPrice, currency),
            convertedCompareAtPrice = convertPrice(rawCompareAtPrice, currency),
        )
    }

    private fun ProductDetailState.withProductLoadError(message: String?): ProductDetailState =
        copy(
            isLoading = false,
            error = message ?: "Something went wrong. Please try again.",
        )

    private fun ProductDetailState.withSelectedVariant(variantId: Long): ProductDetailState {
        val variant = product?.variants?.find { it.id == variantId }
        val rawPrice = variant?.price ?: product?.price
        val rawCompareAtPrice = variant?.compareAtPrice ?: product?.compareAtPrice

        return copy(
            selectedVariantId = variantId,
            showVariantRequiredError = false,
            showSizeRequiredError = false,
            convertedPrice = convertPrice(rawPrice, currency),
            convertedCompareAtPrice = convertPrice(rawCompareAtPrice, currency),
        )
    }

    private fun ProductDetailState.withVariantRequiredError(): ProductDetailState =
        copy(
            showVariantRequiredError = true,
            showSizeRequiredError = true,
        )

    private fun ProductDetailState.withWishlistItem(wishlistItemId: String?): ProductDetailState =
        copy(
            wishlistItemId = wishlistItemId,
            isWishlisted = wishlistItemId != null,
            isWishlistUpdating = false,
        )

    private fun ProductDetailState.withWishlistUpdating(isUpdating: Boolean): ProductDetailState =
        copy(isWishlistUpdating = isUpdating)

    // ── Price conversion helpers ───────────────────────────────────────────────

    private fun convertPrice(rawUsd: String?, currency: AppCurrency): Double? {
        val usd = rawUsd?.toDoubleOrNull() ?: return null
        return usd * currency.rateFromUsd
    }

    /** Returns the raw USD price for the currently selected variant or product default. */
    private fun ProductDetailState.rawPrice(): String? {
        val variant = product?.variants?.find { it.id == selectedVariantId }
        return variant?.price ?: product?.price
    }

    private fun ProductDetailState.rawCompareAtPrice(): String? {
        val variant = product?.variants?.find { it.id == selectedVariantId }
        return variant?.compareAtPrice ?: product?.compareAtPrice
    }
}
