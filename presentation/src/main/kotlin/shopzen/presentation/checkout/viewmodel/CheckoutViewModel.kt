package shopzen.presentation.checkout.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.usecase.ClearCartUseCase
import shopzen.domain.cart.usecase.GetCartUseCase
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.checkout.usecase.GetAvailablePaymentMethodsUseCase
import shopzen.domain.checkout.usecase.PlaceOrderUseCase
import shopzen.domain.profile.model.Address
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.presentation.R
import shopzen.presentation.checkout.intent.CheckoutIntent
import shopzen.presentation.checkout.state.CheckoutItemUi
import shopzen.presentation.checkout.state.CheckoutState
import shopzen.presentation.common.util.UiText

sealed class CheckoutEffect {
    data object NavigateToLogin : CheckoutEffect()
    data object NavigateToAddAddress : CheckoutEffect()
    data object NavigateToPayment : CheckoutEffect()
    data class NavigateToOrderConfirmation(
        val orderId: String,
        val orderNumber: String,
    ) : CheckoutEffect()
    data object NavigateHome : CheckoutEffect()
    data class ShowSnackbar(val message: UiText) : CheckoutEffect()
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val getAvailablePaymentMethodsUseCase: GetAvailablePaymentMethodsUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private val effectsChannel = Channel<CheckoutEffect>(Channel.BUFFERED)
    val effects = effectsChannel.receiveAsFlow()

    private var userId: String? = null
    private var currentCart: Cart? = null

    fun processIntent(intent: CheckoutIntent) {
        Log.d(TAG, "processIntent=$intent")
        when (intent) {
            CheckoutIntent.LoadCheckout -> loadCheckout()
            is CheckoutIntent.SelectShippingAddress -> _state.update {
                it.copy(selectedAddressId = intent.addressId, error = null)
            }
            CheckoutIntent.AddAddressClicked -> viewModelScope.launch {
                effectsChannel.send(CheckoutEffect.NavigateToAddAddress)
            }
            CheckoutIntent.ContinueToPayment -> continueToPayment()
            is CheckoutIntent.SelectPaymentMethod -> _state.update {
                it.copy(selectedPaymentMethod = intent.method, error = null)
            }
            CheckoutIntent.RequestPlaceOrder -> requestPlaceOrder()
            CheckoutIntent.ConfirmPlaceOrder -> confirmPlaceOrder()
            CheckoutIntent.DismissPlaceOrderDialog -> _state.update {
                it.copy(showPlaceOrderDialog = false)
            }
            CheckoutIntent.Retry -> loadCheckout()
            CheckoutIntent.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadCheckout() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, error = null) }

            val user = getCurrentUserUseCase().getOrNull()
            if (user == null) {
                Log.w(TAG, "loadCheckout: no current user, navigate login")
                _state.update { it.copy(isLoading = false) }
                effectsChannel.send(CheckoutEffect.NavigateToLogin)
                return@launch
            }
            userId = user.uid
            Log.d(TAG, "loadCheckout: user resolved uid=${user.uid}")

            val cartResult = getCartUseCase(user.uid).first()
            val cart = cartResult.getOrElse {
                Log.e(TAG, "loadCheckout: cart load failed", it)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.checkout_error_load_failed),
                    )
                }
                return@launch
            }

            val addressesResult = getSavedAddressesUseCase(user.uid).first()
            val addresses = addressesResult.getOrElse {
                Log.e(TAG, "loadCheckout: addresses load failed", it)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.checkout_error_load_failed),
                    )
                }
                return@launch
            }

            currentCart = cart
            Log.d(
                TAG,
                "loadCheckout: cart items=${cart.items.size}, total=${cart.totalPrice}, " +
                    "currency=${cart.currency}, discount=${cart.discountAmount}, addresses=${addresses.size}",
            )
            _state.update { current ->
                val selectedAddressId = selectAddressId(
                    addresses = addresses,
                    currentSelectedAddressId = current.selectedAddressId,
                )
                val availableMethods = getAvailablePaymentMethodsUseCase(cart.totalPrice)
                val selectedPaymentMethod = current.selectedPaymentMethod
                    ?.takeIf { it in availableMethods }
                    ?: availableMethods.firstOrNull()
                Log.d(
                    TAG,
                    "loadCheckout: selectedAddressId=$selectedAddressId, " +
                        "availableMethods=$availableMethods, selectedPayment=$selectedPaymentMethod",
                )

                current.copy(
                    isLoading = false,
                    error = null,
                    items = cart.items.map { it.toUi(cart.currency) },
                    addresses = addresses,
                    selectedAddressId = selectedAddressId,
                    availablePaymentMethods = availableMethods,
                    selectedPaymentMethod = selectedPaymentMethod,
                    formattedSubtotal = cart.currency.formatPrice(cart.subtotalPrice),
                    formattedDiscount = cart.discountAmount.takeIf { it > 0.0 }
                        ?.let { "-${cart.currency.formatPrice(it)}" },
                    formattedTotal = cart.currency.formatPrice(cart.totalPrice),
                    appliedCouponLabel = cart.appliedCoupon?.code,
                )
            }
        }
    }

    private fun continueToPayment() {
        val current = _state.value
        when {
            current.items.isEmpty() -> _state.update {
                it.copy(error = UiText.StringResource(R.string.checkout_error_cart_empty))
            }
            current.selectedAddress == null -> _state.update {
                it.copy(error = UiText.StringResource(R.string.checkout_error_address_required))
            }
            else -> viewModelScope.launch {
                effectsChannel.send(CheckoutEffect.NavigateToPayment)
            }
        }
    }

    private fun requestPlaceOrder() {
        val error = validateOrderState()
        if (error != null) {
            Log.w(TAG, "requestPlaceOrder: validation failed=$error")
            _state.update { it.copy(error = error) }
            return
        }
        Log.d(TAG, "requestPlaceOrder: validation passed, showing dialog")
        _state.update { it.copy(showPlaceOrderDialog = true, error = null) }
    }

    private fun confirmPlaceOrder() {
        val uid = userId ?: run {
            Log.e(TAG, "confirmPlaceOrder: missing userId")
            return
        }
        val cart = currentCart ?: run {
            Log.e(TAG, "confirmPlaceOrder: missing currentCart")
            return
        }
        val address = _state.value.selectedAddress
        val paymentMethod = _state.value.selectedPaymentMethod
        val error = validateOrderState(address = address, paymentMethod = paymentMethod)
        if (error != null) {
            Log.w(TAG, "confirmPlaceOrder: validation failed=$error")
            _state.update {
                it.copy(
                    error = error,
                    showPlaceOrderDialog = false,
                    isPlacingOrder = false,
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.update {
                it.copy(
                    isPlacingOrder = true,
                    showPlaceOrderDialog = false,
                    error = null,
                )
            }

            val checkout = Checkout(
                lineItems = cart.items,
                shippingAddress = address ?: return@launch,
                subtotalPrice = cart.subtotalPrice,
                discountAmount = cart.discountAmount,
                totalPrice = cart.totalPrice,
                currency = cart.currency,
                appliedCoupon = cart.appliedCoupon,
                selectedPaymentMethod = paymentMethod,
            )
            Log.d(
                TAG,
                "confirmPlaceOrder: placing order items=${checkout.lineItems.size}, " +
                    "total=${checkout.totalPrice}, currency=${checkout.currency}, " +
                    "addressId=${checkout.shippingAddress.id}, payment=${checkout.selectedPaymentMethod}, " +
                    "coupon=${checkout.appliedCoupon?.code}",
            )

            placeOrderUseCase(checkout).fold(
                onSuccess = { confirmation ->
                    Log.d(
                        TAG,
                        "confirmPlaceOrder: order success id=${confirmation.orderId}, " +
                            "number=${confirmation.orderNumber}",
                    )
                    val clearResult = clearCartUseCase(uid)
                    _state.update { it.copy(isPlacingOrder = false) }
                    if (clearResult.isFailure) {
                        Log.e(TAG, "confirmPlaceOrder: clear cart failed", clearResult.exceptionOrNull())
                        effectsChannel.send(
                            CheckoutEffect.ShowSnackbar(
                                UiText.StringResource(R.string.checkout_error_clear_cart_after_order),
                            )
                        )
                    }
                    effectsChannel.send(
                        CheckoutEffect.NavigateToOrderConfirmation(
                            orderId = confirmation.orderId,
                            orderNumber = confirmation.orderNumber,
                        )
                    )
                },
                onFailure = { throwable ->
                    Log.e(TAG, "confirmPlaceOrder: place order failed", throwable)
                    _state.update {
                        it.copy(
                            isPlacingOrder = false,
                            error = UiText.StringResource(R.string.checkout_error_order_failed),
                        )
                    }
                },
            )
        }
    }

    private fun validateOrderState(
        address: Address? = _state.value.selectedAddress,
        paymentMethod: PaymentMethod? = _state.value.selectedPaymentMethod,
    ): UiText? = when {
        currentCart?.items.isNullOrEmpty() -> UiText.StringResource(R.string.checkout_error_cart_empty)
        address == null -> UiText.StringResource(R.string.checkout_error_address_required)
        paymentMethod == null -> UiText.StringResource(R.string.checkout_error_payment_required)
        else -> null
    }

    private fun selectAddressId(
        addresses: List<Address>,
        currentSelectedAddressId: String?,
    ): String? =
        currentSelectedAddressId?.takeIf { selected ->
            addresses.any { it.id == selected }
        } ?: addresses.firstOrNull { it.isDefault }?.id
        ?: addresses.firstOrNull()?.id

    private fun CartItem.toUi(currency: String): CheckoutItemUi =
        CheckoutItemUi(
            id = id,
            title = title,
            variantTitle = variantTitle,
            formattedUnitPrice = currency.formatPrice(price),
            quantity = quantity,
            formattedLineTotal = currency.formatPrice(price * quantity),
            imageUrl = imageUrl,
        )

    private fun String.formatPrice(amount: Double): String =
        "${currencySymbol()}%.2f".format(amount)

    private fun String.currencySymbol(): String =
        when (uppercase()) {
            "USD" -> "$"
            "EGP" -> "EGP "
            else -> "$"
        }

    private companion object {
        const val TAG = "CheckoutViewModel"
    }
}
