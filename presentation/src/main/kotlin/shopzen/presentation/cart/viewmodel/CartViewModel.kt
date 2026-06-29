package shopzen.presentation.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.CouponValidationResult
import shopzen.domain.cart.usecase.ClearCartUseCase
import shopzen.domain.cart.usecase.GetCartTotalUseCase
import shopzen.domain.cart.usecase.GetCartUseCase
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
import shopzen.domain.cart.usecase.RemoveFromCartUseCase
import shopzen.domain.cart.usecase.UpdateCartItemQuantityUseCase
import shopzen.domain.cart.usecase.ValidateCouponUseCase
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.state.CartItemUi
import shopzen.presentation.cart.state.CartState
import javax.inject.Inject

/** One-shot navigation / snackbar events emitted to the state-holder composable. */
sealed class CartEffect {
    data class NavigateToProduct(val productId: String) : CartEffect()
    data object NavigateToCheckout : CartEffect()
    data class ShowSnackbar(val message: String) : CartEffect()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val validateCouponUseCase: ValidateCouponUseCase,
    private val getCartTotalUseCase: GetCartTotalUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
) : ViewModel() {

    // Mock userId — replaced by auth session once wired
    private val userId = "mock-user-id"

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CartEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    // Tracks raw domain items for quantity mutation without re-fetching
    private var rawItems: List<CartItem> = emptyList()
    private var discountMultiplier: Double = 1.0  // 1.0 = no discount

    init {
        loadCurrencyAndCart()
    }

    // ── Public entry point ─────────────────────────────────────────────────────

    fun processIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.IncrementQuantity -> handleIncrement(intent.itemId)
            is CartIntent.DecrementQuantity -> handleDecrement(intent.itemId)
            is CartIntent.RequestRemoveItem -> _state.update {
                it.copy(showRemoveItemDialog = true, pendingRemovalItemId = intent.itemId)
            }
            CartIntent.ConfirmRemoveItem -> confirmRemoveItem()
            CartIntent.DismissRemoveItemDialog -> _state.update {
                it.copy(showRemoveItemDialog = false, pendingRemovalItemId = null)
            }
            CartIntent.RequestClearCart -> _state.update { it.copy(showClearCartDialog = true) }
            CartIntent.ConfirmClearCart -> confirmClearCart()
            CartIntent.DismissClearCartDialog -> _state.update { it.copy(showClearCartDialog = false) }
            CartIntent.OpenCouponSheet -> _state.update { it.copy(showCouponSheet = true) }
            CartIntent.DismissCouponSheet -> _state.update { it.copy(showCouponSheet = false) }
            is CartIntent.UpdateCouponCode -> _state.update {
                it.copy(couponCode = intent.code, couponError = null)
            }
            CartIntent.ApplyCoupon -> handleApplyCoupon()
            CartIntent.RemoveCoupon -> handleRemoveCoupon()
            CartIntent.Checkout -> viewModelScope.launch {
                _effects.emit(CartEffect.NavigateToCheckout)
            }
            is CartIntent.NavigateToProduct -> viewModelScope.launch {
                _effects.emit(CartEffect.NavigateToProduct(intent.productId))
            }
            CartIntent.Retry -> loadCurrencyAndCart()
        }
    }

    // ── Private handlers ───────────────────────────────────────────────────────

    private fun loadCurrencyAndCart() {
        viewModelScope.launch {
            val symbol = getCurrencySymbolUseCase()
            _state.update { it.copy(currencySymbol = symbol) }
            observeCart(symbol)
        }
    }

    private fun observeCart(symbol: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        getCartUseCase(userId)
            .onEach { result ->
                result.fold(
                    onSuccess = { cart ->
                        rawItems = cart.items
                        _state.update { current ->
                            val uiItems = cart.items.map { it.toUi(symbol) }
                            val subtotal = getCartTotalUseCase(cart.items)
                            val discounted = subtotal * discountMultiplier
                            current.copy(
                                isLoading = false,
                                error = null,
                                items = uiItems,
                                formattedSubtotal = symbol.formatPrice(subtotal),
                                formattedTotal = symbol.formatPrice(discounted),
                                formattedDiscount = if (discountMultiplier < 1.0) {
                                    "-${symbol.formatPrice(subtotal - discounted)}"
                                } else null,
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _state.update {
                            it.copy(isLoading = false, error = throwable.message ?: "Failed to load cart")
                        }
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun handleIncrement(itemId: String) {
        val item = rawItems.find { it.id == itemId } ?: return
        if (item.quantity >= item.maxQuantity) return
        updateQuantity(itemId, item.quantity + 1)
    }

    private fun handleDecrement(itemId: String) {
        val item = rawItems.find { it.id == itemId } ?: return
        if (item.quantity <= 1) return
        updateQuantity(itemId, item.quantity - 1)
    }

    private fun updateQuantity(itemId: String, newQty: Int) {
        // Optimistic local update
        rawItems = rawItems.map { if (it.id == itemId) it.copy(quantity = newQty) else it }
        val symbol = _state.value.currencySymbol
        val subtotal = getCartTotalUseCase(rawItems)
        val discounted = subtotal * discountMultiplier
        _state.update { current ->
            current.copy(
                items = rawItems.map { it.toUi(symbol) },
                formattedSubtotal = symbol.formatPrice(subtotal),
                formattedTotal = symbol.formatPrice(discounted),
                formattedDiscount = if (discountMultiplier < 1.0) {
                    "-${symbol.formatPrice(subtotal - discounted)}"
                } else null,
            )
        }
        // Persist in background
        viewModelScope.launch {
            updateCartItemQuantityUseCase(itemId, newQty, userId)
        }
    }

    private fun confirmRemoveItem() {
        val itemId = _state.value.pendingRemovalItemId ?: return
        _state.update { it.copy(showRemoveItemDialog = false, pendingRemovalItemId = null) }
        // Optimistic remove
        rawItems = rawItems.filter { it.id != itemId }
        val symbol = _state.value.currencySymbol
        val subtotal = getCartTotalUseCase(rawItems)
        val discounted = subtotal * discountMultiplier
        _state.update { current ->
            current.copy(
                items = rawItems.map { it.toUi(symbol) },
                formattedSubtotal = symbol.formatPrice(subtotal),
                formattedTotal = symbol.formatPrice(discounted),
                formattedDiscount = if (discountMultiplier < 1.0) {
                    "-${symbol.formatPrice(subtotal - discounted)}"
                } else null,
            )
        }
        viewModelScope.launch {
            removeFromCartUseCase(itemId, userId)
        }
    }

    private fun confirmClearCart() {
        _state.update { it.copy(showClearCartDialog = false) }
        rawItems = emptyList()
        discountMultiplier = 1.0
        val symbol = _state.value.currencySymbol
        _state.update { current ->
            current.copy(
                items = emptyList(),
                formattedSubtotal = symbol.formatPrice(0.0),
                formattedTotal = symbol.formatPrice(0.0),
                formattedDiscount = null,
                couponApplied = false,
                appliedCouponLabel = null,
            )
        }
        viewModelScope.launch { clearCartUseCase(userId) }
    }

    private fun handleApplyCoupon() {
        val code = _state.value.couponCode.trim()
        if (code.isBlank()) {
            _state.update { it.copy(couponError = "Please enter a coupon code") }
            return
        }
        _state.update { it.copy(isCouponLoading = true, couponError = null) }
        viewModelScope.launch {
            validateCouponUseCase(code).fold(
                onSuccess = { result ->
                    when (result) {
                        is CouponValidationResult.Valid -> {
                            val pct = result.discountPercent
                            val fixed = result.discountFixed
                            discountMultiplier = when {
                                pct != null -> 1.0 - (pct / 100.0)
                                fixed != null -> {
                                    val subtotal = getCartTotalUseCase(rawItems)
                                    if (subtotal > 0) 1.0 - (fixed / subtotal) else 1.0
                                }
                                else -> 1.0
                            }
                            val symbol = _state.value.currencySymbol
                            val subtotal = getCartTotalUseCase(rawItems)
                            val discounted = subtotal * discountMultiplier
                            val label = when {
                                pct != null -> "${result.code} (-${pct.toInt()}%)"
                                fixed != null -> "${result.code} (-${symbol.formatPrice(fixed)})"
                                else -> result.code
                            }
                            _state.update { current ->
                                current.copy(
                                    isCouponLoading = false,
                                    couponApplied = true,
                                    appliedCouponLabel = label,
                                    couponError = null,
                                    showCouponSheet = false,
                                    formattedSubtotal = symbol.formatPrice(subtotal),
                                    formattedDiscount = "-${symbol.formatPrice(subtotal - discounted)}",
                                    formattedTotal = symbol.formatPrice(discounted),
                                )
                            }
                            _effects.emit(CartEffect.ShowSnackbar("Coupon applied!"))
                        }
                        is CouponValidationResult.Invalid -> {
                            _state.update {
                                it.copy(isCouponLoading = false, couponError = result.reason)
                            }
                        }
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isCouponLoading = false,
                            couponError = throwable.message ?: "Could not validate coupon",
                        )
                    }
                }
            )
        }
    }

    private fun handleRemoveCoupon() {
        discountMultiplier = 1.0
        val symbol = _state.value.currencySymbol
        val subtotal = getCartTotalUseCase(rawItems)
        _state.update { current ->
            current.copy(
                couponApplied = false,
                appliedCouponLabel = null,
                couponCode = "",
                couponError = null,
                formattedSubtotal = symbol.formatPrice(subtotal),
                formattedDiscount = null,
                formattedTotal = symbol.formatPrice(subtotal),
            )
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun CartItem.toUi(symbol: String) = CartItemUi(
        id = id,
        productId = productId,
        variantId = variantId,
        title = title,
        variantTitle = variantTitle,
        formattedPrice = symbol.formatPrice(price),
        quantity = quantity,
        maxQuantity = maxQuantity,
        imageUrl = imageUrl,
    )

    private fun String.formatPrice(amount: Double): String =
        "${this}%.2f".format(amount)
}
