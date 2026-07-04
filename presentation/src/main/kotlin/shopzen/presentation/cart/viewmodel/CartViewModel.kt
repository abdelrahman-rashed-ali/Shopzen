package shopzen.presentation.cart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.CouponValidationResult
import shopzen.domain.cart.model.DiscountCode
import shopzen.domain.cart.model.DiscountType
import shopzen.domain.cart.usecase.AddToCartUseCase
import shopzen.domain.cart.usecase.ApplyCouponUseCase
import shopzen.domain.cart.usecase.ClearCartUseCase
import shopzen.domain.cart.usecase.GetCartTotalUseCase
import shopzen.domain.cart.usecase.GetCartUseCase
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
import shopzen.domain.cart.usecase.RemoveCouponUseCase
import shopzen.domain.cart.usecase.RemoveFromCartUseCase
import shopzen.domain.cart.usecase.UpdateCartItemQuantityUseCase

import shopzen.presentation.R
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.state.CartItemUi
import shopzen.presentation.cart.state.CartState
import shopzen.presentation.common.util.UiText
import javax.inject.Inject

sealed class CartEffect {
    data class NavigateToProduct(val productId: String) : CartEffect()
    data object NavigateToCheckout : CartEffect()
    data object NavigateToLogin : CartEffect()
    data class ShowSnackbar(val message: UiText) : CartEffect()
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getCartTotalUseCase: GetCartTotalUseCase,
    private val applyCouponUseCase: ApplyCouponUseCase,
    private val removeCouponUseCase: RemoveCouponUseCase,
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val addToCartUseCase: AddToCartUseCase,
) : ViewModel() {

    /**
     * Firebase UID resolved once at init from [GetCurrentUserUseCase].
     * Null when the auth guard failed to prevent unauthenticated access.
     */
    private var userId: String? = null

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CartEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    private var currentCart: Cart? = null

    init {
        viewModelScope.launch {
            val userResult = getCurrentUserUseCase()
            val user = userResult.getOrNull()
            if (user == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.cart_error_not_authenticated),
                    )
                }
            } else {
                userId = user.uid
                loadCurrencyAndCart()
            }
        }
    }

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
            CartIntent.Refresh -> loadCurrencyAndCart(forceRefresh = true)
            is CartIntent.AddToCart -> handleAddToCart(intent)
        }
    }

    private fun loadCurrencyAndCart(forceRefresh: Boolean = false) {
        val uid = userId ?: return
        viewModelScope.launch {
            val symbol = getCurrencySymbolUseCase()
            _state.update { it.copy(currencySymbol = symbol) }
            observeCart(uid, symbol, forceRefresh)
        }
    }

    private var observeJob: kotlinx.coroutines.Job? = null

    private fun observeCart(uid: String, symbol: String, forceRefresh: Boolean = false) {
        observeJob?.cancel()
        if (forceRefresh) {
            _state.update { it.copy(isRefreshing = true, error = null) }
        } else {
            _state.update { it.copy(isLoading = true, error = null) }
        }
        observeJob = getCartUseCase(uid, forceRefresh)
            .onEach { result ->
                result.fold(
                    onSuccess = { cart ->
                        currentCart = cart
                        _state.update { current ->
                            val uiItems = cart.items.map { it.toUi(symbol) }
                            current.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = null,
                                items = uiItems,
                                formattedSubtotal = symbol.formatPrice(cart.subtotalPrice),
                                formattedTotal = symbol.formatPrice(cart.totalPrice),
                                formattedDiscount = if (cart.discountAmount > 0) {
                                    "-${symbol.formatPrice(cart.discountAmount)}"
                                } else null,
                                couponApplied = cart.appliedCoupon != null,
                                appliedCouponLabel = cart.appliedCoupon?.code?.let { "$it" }, // Shopify UI just shows code
                                couponCode = cart.appliedCoupon?.code ?: "",
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = throwable.message?.let { msg -> UiText.DynamicString(msg) as UiText }
                                    ?: UiText.StringResource(R.string.cart_error_load_failed)
                            )
                        }
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    private fun handleIncrement(itemId: String) {
        val cart = currentCart ?: return
        val item = cart.items.find { it.id == itemId } ?: return
        if (item.quantity >= item.maxQuantity) return
        updateQuantity(itemId, item.quantity + 1)
    }

    private fun handleDecrement(itemId: String) {
        val cart = currentCart ?: return
        val item = cart.items.find { it.id == itemId } ?: return
        if (item.quantity <= 1) return
        updateQuantity(itemId, item.quantity - 1)
    }

    private fun updateQuantity(itemId: String, newQty: Int) {
        val cart = currentCart ?: return
        val updatedItems = cart.items.map { if (it.id == itemId) it.copy(quantity = newQty) else it }
        val updatedCart = cart.copy(items = updatedItems, subtotalPrice = getCartTotalUseCase(updatedItems))
        currentCart = updatedCart
        
        val symbol = _state.value.currencySymbol
        _state.update { current ->
            current.copy(
                items = updatedCart.items.map { it.toUi(symbol) },
                formattedSubtotal = symbol.formatPrice(updatedCart.subtotalPrice),
                formattedTotal = symbol.formatPrice(updatedCart.totalPrice),
                formattedDiscount = if (updatedCart.discountAmount > 0) {
                    "-${symbol.formatPrice(updatedCart.discountAmount)}"
                } else null,
            )
        }
        viewModelScope.launch {
            updateCartItemQuantityUseCase(itemId, newQty, userId ?: return@launch).onFailure { throwable ->
                // Revert optimistic UI by reloading
                loadCurrencyAndCart()
                _effects.emit(
                    CartEffect.ShowSnackbar(
                        throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.cart_error_load_failed)
                    )
                )
            }
        }
    }

    private fun confirmRemoveItem() {
        val itemId = _state.value.pendingRemovalItemId ?: return
        _state.update { it.copy(showRemoveItemDialog = false, pendingRemovalItemId = null) }
        
        val cart = currentCart ?: return
        val updatedItems = cart.items.filter { it.id != itemId }
        val updatedCart = cart.copy(items = updatedItems, subtotalPrice = getCartTotalUseCase(updatedItems))
        currentCart = updatedCart
        
        val symbol = _state.value.currencySymbol
        _state.update { current ->
            current.copy(
                items = updatedCart.items.map { it.toUi(symbol) },
                formattedSubtotal = symbol.formatPrice(updatedCart.subtotalPrice),
                formattedTotal = symbol.formatPrice(updatedCart.totalPrice),
                formattedDiscount = if (updatedCart.discountAmount > 0) {
                    "-${symbol.formatPrice(updatedCart.discountAmount)}"
                } else null,
            )
        }
        viewModelScope.launch {
            removeFromCartUseCase(itemId, userId ?: return@launch).onFailure { throwable ->
                // Revert optimistic UI by reloading
                loadCurrencyAndCart()
                _effects.emit(
                    CartEffect.ShowSnackbar(
                        throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.cart_error_load_failed)
                    )
                )
            }
        }
    }

    private fun confirmClearCart() {
        _state.update { it.copy(showClearCartDialog = false) }
        currentCart = null
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
        viewModelScope.launch {
            clearCartUseCase(userId ?: return@launch).onFailure { throwable ->
                // Revert optimistic UI by reloading
                loadCurrencyAndCart()
                _effects.emit(
                    CartEffect.ShowSnackbar(
                        throwable.message?.let(UiText::DynamicString)
                            ?: UiText.StringResource(R.string.cart_error_load_failed)
                    )
                )
            }
        }
    }

    private fun handleApplyCoupon() {
        val uid = userId ?: return
        val code = _state.value.couponCode.trim()
        if (code.isBlank()) {
            _state.update { it.copy(couponError = UiText.StringResource(R.string.cart_coupon_empty_error)) }
            return
        }
        _state.update { it.copy(isCouponLoading = true, couponError = null) }
        viewModelScope.launch(Dispatchers.IO) {
            applyCouponUseCase(userId = uid, code = code).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isCouponLoading = false,
                            couponError = null,
                            showCouponSheet = false,
                        )
                    }
                    _effects.emit(CartEffect.ShowSnackbar(UiText.StringResource(R.string.cart_coupon_applied)))
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isCouponLoading = false,
                            couponError = throwable.message?.let { msg -> UiText.DynamicString(msg) }
                                ?: UiText.StringResource(R.string.cart_coupon_validate_error),
                        )
                    }
                }
            )
        }
    }

    private fun handleRemoveCoupon() {
        val uid = userId ?: return
        val code = _state.value.couponCode
        if (code.isBlank()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true) }
            removeCouponUseCase(userId = uid, code = code).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            couponCode = "",
                            couponApplied = false,
                            appliedCouponLabel = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update { it.copy(isLoading = false) }
                    _effects.emit(
                        CartEffect.ShowSnackbar(
                            throwable.message?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.cart_error_add_failed)
                        )
                    )
                }
            )
        }
    }

    private fun handleAddToCart(intent: CartIntent.AddToCart) {
        val currentUserId = userId
        if (currentUserId == null) {
            viewModelScope.launch {
                _effects.emit(CartEffect.NavigateToLogin)
            }
            return
        }

        val item = CartItem(
            id = intent.variantId, // Shopify will generate the real CartLine ID
            productId = intent.productId,
            variantId = intent.variantId,
            title = intent.title,
            variantTitle = intent.variantTitle,
            price = intent.price,
            quantity = 1,
            maxQuantity = intent.maxQuantity,
            imageUrl = intent.imageUrl,
            userId = currentUserId
        )

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            addToCartUseCase(item).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }

                    _effects.emit(
                        CartEffect.ShowSnackbar(
                            UiText.StringResource(R.string.cart_item_added)
                        )
                    )
                },
                onFailure = { throwable ->
                    _state.update { it.copy(isLoading = false) }

                    _effects.emit(
                        CartEffect.ShowSnackbar(
                            throwable.message?.let(UiText::DynamicString)
                                ?: UiText.StringResource(R.string.cart_error_add_failed)
                        )
                    )
                }
            )
        }
    }

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
