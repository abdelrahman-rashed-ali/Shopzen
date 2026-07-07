package shopzen.presentation.checkout.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
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
import shopzen.domain.checkout.usecase.CreatePaymobPaymentIntentionUseCase
import shopzen.domain.checkout.usecase.GetAvailablePaymentMethodsUseCase
import shopzen.domain.checkout.usecase.PlaceOrderUseCase
import shopzen.domain.customer.usecase.GetCurrentShopifyCustomerIdUseCase
import shopzen.domain.profile.model.Address
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.presentation.R
import shopzen.presentation.checkout.intent.CheckoutIntent
import shopzen.presentation.checkout.state.CheckoutItemUi
import shopzen.presentation.checkout.state.CheckoutState
import shopzen.presentation.checkout.state.PendingPaymobLaunch
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

private enum class PaymobPaymentStatus {
    SUCCESS,
    PENDING,
    FAILED,
    UNKNOWN,
}

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getCurrentShopifyCustomerIdUseCase: GetCurrentShopifyCustomerIdUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val getAvailablePaymentMethodsUseCase: GetAvailablePaymentMethodsUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val createPaymobPaymentIntentionUseCase: CreatePaymobPaymentIntentionUseCase,
    private val clearCartUseCase: ClearCartUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    private val effectsChannel = Channel<CheckoutEffect>(Channel.BUFFERED)
    val effects = effectsChannel.receiveAsFlow()

    private var userId: String? = null
    private var currentShopifyCustomerId: Long? = null
    private var currentUserEmail: String = ""
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
            is CheckoutIntent.ConsumePendingPaymobLaunch -> _state.update { current ->
                if (current.pendingPaymobLaunch?.intentionId == intent.intentionId) {
                    current.copy(pendingPaymobLaunch = null)
                } else {
                    current
                }
            }
            is CheckoutIntent.OnlinePaymentSucceeded -> handleOnlinePaymentSucceeded(intent.payResponse)
            is CheckoutIntent.OnlinePaymentFailed -> handleOnlinePaymentFailed(intent.message)
            CheckoutIntent.OnlinePaymentPending -> handleOnlinePaymentPending()
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
            currentUserEmail = user.email
            Log.d(TAG, "loadCheckout: user resolved uid=${user.uid}")

            val customerId = getCurrentShopifyCustomerIdUseCase().getOrElse {
                Log.e(TAG, "loadCheckout: Shopify customer id load failed", it)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.checkout_error_customer_required),
                    )
                }
                return@launch
            }
            currentShopifyCustomerId = customerId

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
                    isOnlinePaymentInProgress = false,
                    pendingPaymobLaunch = null,
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
        val current = _state.value
        if (current.isPlacingOrder || current.isOnlinePaymentInProgress || current.showPlaceOrderDialog) {
            Log.d(TAG, "requestPlaceOrder: ignored while checkout action already in progress")
            return
        }
        val error = validateOrderState() ?: validateOnlinePaymentState()
        if (error != null) {
            Log.w(TAG, "requestPlaceOrder: validation failed=$error")
            _state.update { it.copy(error = error) }
            return
        }
        Log.d(TAG, "requestPlaceOrder: validation passed, showing dialog")
        _state.update { it.copy(showPlaceOrderDialog = true, error = null) }
    }

    private fun confirmPlaceOrder() {
        val currentState = _state.value
        if (!currentState.showPlaceOrderDialog ||
            currentState.isPlacingOrder ||
            currentState.isOnlinePaymentInProgress
        ) {
            Log.d(TAG, "confirmPlaceOrder: ignored duplicate or stale confirm")
            return
        }
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
            ?: validateOnlinePaymentState(address = address, paymentMethod = paymentMethod)
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

        val checkout = buildCheckout(
            cart = cart,
            address = address ?: return,
            paymentMethod = paymentMethod,
        )
        Log.d(
            TAG,
            "confirmPlaceOrder: confirmed checkout items=${checkout.lineItems.size}, " +
                "total=${checkout.totalPrice}, currency=${checkout.currency}, " +
                "addressId=${checkout.shippingAddress.id}, payment=${checkout.selectedPaymentMethod}, " +
                "coupon=${checkout.appliedCoupon?.code}",
        )
        _state.update {
            it.copy(
                isPlacingOrder = true,
                isOnlinePaymentInProgress = false,
                pendingPaymobLaunch = null,
                showPlaceOrderDialog = false,
                error = null,
            )
        }

        if (paymentMethod == PaymentMethod.ONLINE_PAYMENT) {
            startOnlinePayment(checkout, alreadySubmitting = true)
        } else {
            placeConfirmedOrder(
                uid = uid,
                checkout = checkout,
                alreadySubmitting = true,
            )
        }
    }

    private fun startOnlinePayment(
        checkout: Checkout,
        alreadySubmitting: Boolean = false,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!alreadySubmitting) {
                _state.update {
                    it.copy(
                        isPlacingOrder = true,
                        isOnlinePaymentInProgress = false,
                        pendingPaymobLaunch = null,
                        showPlaceOrderDialog = false,
                        error = null,
                    )
                }
            }
            createPaymobPaymentIntentionUseCase(checkout).fold(
                onSuccess = { intention ->
                    Log.d(
                        TAG,
                        "startOnlinePayment: intention=${intention.intentionId}, " +
                            "order=${intention.intentionOrderId}",
                    )
                    _state.update {
                        it.copy(
                            isPlacingOrder = false,
                            isOnlinePaymentInProgress = true,
                            pendingPaymobLaunch = PendingPaymobLaunch(
                                intentionId = intention.intentionId,
                                clientSecret = intention.clientSecret,
                                publicKey = intention.publicKey,
                            ),
                        )
                    }
                },
                onFailure = { throwable ->
                    Log.e(TAG, "startOnlinePayment: failed", throwable)
                    val error = if (throwable.message?.contains("integration IDs", ignoreCase = true) == true) {
                        UiText.StringResource(R.string.checkout_error_paymob_config_missing)
                    } else {
                        UiText.StringResource(R.string.checkout_error_payment_start_failed)
                    }
                    _state.update {
                        it.copy(
                            isPlacingOrder = false,
                            isOnlinePaymentInProgress = false,
                            pendingPaymobLaunch = null,
                            error = error,
                        )
                    }
                },
            )
        }
    }

    private fun handleOnlinePaymentSucceeded(payResponse: Map<String, String?>) {
        Log.d(TAG, "handleOnlinePaymentSucceeded: responseKeys=${payResponse.keys}")
        when (payResponse.toPaymobPaymentStatus()) {
            PaymobPaymentStatus.PENDING -> {
                handleOnlinePaymentPending()
                return
            }
            PaymobPaymentStatus.FAILED -> {
                handleOnlinePaymentFailed(payResponse.toPaymobRawText())
                return
            }
            PaymobPaymentStatus.SUCCESS,
            PaymobPaymentStatus.UNKNOWN -> Unit
        }
        val uid = userId ?: return
        val cart = currentCart ?: return
        val address = _state.value.selectedAddress ?: return
        val checkout = buildCheckout(
            cart = cart,
            address = address,
            paymentMethod = PaymentMethod.ONLINE_PAYMENT,
        )
        placeConfirmedOrder(uid = uid, checkout = checkout)
    }

    private fun handleOnlinePaymentFailed(message: String?) {
        Log.w(TAG, "handleOnlinePaymentFailed: $message")
        when (message.toPaymobPaymentStatus()) {
            PaymobPaymentStatus.SUCCESS -> {
                handleOnlinePaymentSucceeded(mapOf(PAYMOB_RAW_CALLBACK_KEY to message))
                return
            }
            PaymobPaymentStatus.PENDING -> {
                handleOnlinePaymentPending()
                return
            }
            PaymobPaymentStatus.FAILED,
            PaymobPaymentStatus.UNKNOWN -> Unit
        }
        _state.update {
            it.copy(
                isPlacingOrder = false,
                isOnlinePaymentInProgress = false,
                pendingPaymobLaunch = null,
                error = message
                    ?.takeIf { value -> value.isNotBlank() }
                    ?.let { value -> UiText.DynamicString(value) }
                    ?: UiText.StringResource(R.string.checkout_error_payment_failed),
            )
        }
    }

    private fun Map<String, String?>.toPaymobPaymentStatus(): PaymobPaymentStatus {
        val rawText = toPaymobRawText()
        return when {
            hasTruthyValue(PAYMOB_PENDING_KEYS) || rawText.hasTruthyPaymobField(PAYMOB_PENDING_KEYS) -> {
                PaymobPaymentStatus.PENDING
            }
            hasTruthyValue(PAYMOB_SUCCESS_KEYS) || rawText.hasTruthyPaymobField(PAYMOB_SUCCESS_KEYS) -> {
                PaymobPaymentStatus.SUCCESS
            }
            hasFalsyValue(PAYMOB_SUCCESS_KEYS) ||
                rawText.hasFalsyPaymobField(PAYMOB_SUCCESS_KEYS) ||
                hasTruthyValue(PAYMOB_ERROR_KEYS) ||
                rawText.hasTruthyPaymobField(PAYMOB_ERROR_KEYS) -> {
                PaymobPaymentStatus.FAILED
            }
            rawText.hasApprovedPaymobSignal() -> PaymobPaymentStatus.SUCCESS
            rawText.hasRejectedPaymobSignal() -> PaymobPaymentStatus.FAILED
            else -> PaymobPaymentStatus.UNKNOWN
        }
    }

    private fun String?.toPaymobPaymentStatus(): PaymobPaymentStatus {
        if (isNullOrBlank()) return PaymobPaymentStatus.UNKNOWN
        val raw = this
        val candidates = listOf(raw, raw.urlDecoded())
        return when {
            candidates.any { it.hasTruthyPaymobField(PAYMOB_PENDING_KEYS) } -> PaymobPaymentStatus.PENDING
            candidates.any { it.hasTruthyPaymobField(PAYMOB_SUCCESS_KEYS) } &&
                candidates.none { it.hasTruthyPaymobField(PAYMOB_ERROR_KEYS) } -> {
                PaymobPaymentStatus.SUCCESS
            }
            candidates.any { it.hasFalsyPaymobField(PAYMOB_SUCCESS_KEYS) } ||
                candidates.any { it.hasTruthyPaymobField(PAYMOB_ERROR_KEYS) } -> {
                PaymobPaymentStatus.FAILED
            }
            candidates.any { it.hasApprovedPaymobSignal() } -> PaymobPaymentStatus.SUCCESS
            candidates.any { it.hasRejectedPaymobSignal() } -> PaymobPaymentStatus.FAILED
            else -> PaymobPaymentStatus.UNKNOWN
        }
    }

    private fun Map<String, String?>.toPaymobRawText(): String =
        entries.joinToString(separator = "&") { (key, value) -> "$key=${value.orEmpty()}" }

    private fun Map<String, String?>.hasTruthyValue(keys: Set<String>): Boolean =
        hasValue(keys) { it.isTruthyPaymobValue() }

    private fun Map<String, String?>.hasFalsyValue(keys: Set<String>): Boolean =
        hasValue(keys) { it.isFalsyPaymobValue() }

    private fun Map<String, String?>.hasValue(
        keys: Set<String>,
        predicate: (String) -> Boolean,
    ): Boolean =
        entries.any { (key, value) ->
            key.normalizedPaymobKey() in keys && value?.let(predicate) == true
        }

    private fun String?.urlDecoded(): String {
        if (this == null) return ""
        return runCatching {
            URLDecoder.decode(this, StandardCharsets.UTF_8.name())
        }.getOrDefault(this)
    }

    private fun String.hasTruthyPaymobField(keys: Set<String>): Boolean =
        keys.any { key -> hasPaymobFieldValue(key, true) }

    private fun String.hasFalsyPaymobField(keys: Set<String>): Boolean =
        keys.any { key -> hasPaymobFieldValue(key, false) }

    private fun String.hasPaymobFieldValue(key: String, expected: Boolean): Boolean {
        val value = if (expected) "true" else "false"
        val field = Regex.escape(key)
        return Regex(
            pattern = """(?i)(?:["']?\b$field\b["']?\s*[:=]\s*["']?$value["']?)""",
        ).containsMatchIn(this)
    }

    private fun String.isTruthyPaymobValue(): Boolean =
        trim().trim('"', '\'').equals("true", ignoreCase = true)

    private fun String.isFalsyPaymobValue(): Boolean =
        trim().trim('"', '\'').equals("false", ignoreCase = true)

    private fun String.normalizedPaymobKey(): String =
        lowercase().replace("_", "")

    private fun String.hasApprovedPaymobSignal(): Boolean =
        contains("approved", ignoreCase = true) ||
            Regex("""(?i)(?:["']?\btxn_response_code\b["']?\s*[:=]\s*["']?0["']?)""").containsMatchIn(this)

    private fun String.hasRejectedPaymobSignal(): Boolean =
        contains("declined", ignoreCase = true) ||
            contains("rejected", ignoreCase = true) ||
            contains("cancelled", ignoreCase = true) ||
            contains("canceled", ignoreCase = true)

    private fun handleOnlinePaymentPending() {
        Log.d(TAG, "handleOnlinePaymentPending")
        _state.update {
            it.copy(
                isPlacingOrder = false,
                isOnlinePaymentInProgress = false,
                pendingPaymobLaunch = null,
                error = UiText.StringResource(R.string.checkout_payment_pending),
            )
        }
    }

    private fun placeConfirmedOrder(
        uid: String,
        checkout: Checkout,
        alreadySubmitting: Boolean = false,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!alreadySubmitting) {
                _state.update {
                    it.copy(
                        isPlacingOrder = true,
                        isOnlinePaymentInProgress = false,
                        pendingPaymobLaunch = null,
                        showPlaceOrderDialog = false,
                        error = null,
                    )
                }
            }

            placeOrderUseCase(checkout).fold(
                onSuccess = { confirmation ->
                    Log.d(
                        TAG,
                        "placeConfirmedOrder: order success id=${confirmation.orderId}, " +
                            "number=${confirmation.orderNumber}",
                    )
                    val clearResult = clearCartUseCase(uid)
                    _state.update {
                        it.copy(
                            isPlacingOrder = false,
                            isOnlinePaymentInProgress = false,
                            pendingPaymobLaunch = null,
                        )
                    }
                    if (clearResult.isFailure) {
                        Log.e(TAG, "placeConfirmedOrder: clear cart failed", clearResult.exceptionOrNull())
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
                    Log.e(TAG, "placeConfirmedOrder: place order failed", throwable)
                    _state.update {
                        it.copy(
                            isPlacingOrder = false,
                            isOnlinePaymentInProgress = false,
                            pendingPaymobLaunch = null,
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

    private fun validateOnlinePaymentState(
        address: Address? = _state.value.selectedAddress,
        paymentMethod: PaymentMethod? = _state.value.selectedPaymentMethod,
    ): UiText? = when {
        paymentMethod != PaymentMethod.ONLINE_PAYMENT -> null
        currentUserEmail.isBlank() -> UiText.StringResource(R.string.checkout_error_online_payment_email_required)
        address?.phone.isNullOrBlank() -> UiText.StringResource(R.string.checkout_error_online_payment_phone_required)
        else -> null
    }

    private fun buildCheckout(
        cart: Cart,
        address: Address,
        paymentMethod: PaymentMethod?,
    ): Checkout =
        Checkout(
            lineItems = cart.items,
            shippingAddress = address,
            subtotalPrice = cart.subtotalPrice,
            discountAmount = cart.discountAmount,
            totalPrice = cart.totalPrice,
            currency = cart.currency,
            appliedCoupon = cart.appliedCoupon,
            selectedPaymentMethod = paymentMethod,
            customerId = currentShopifyCustomerId,
            customerEmail = currentUserEmail,
        )

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
        const val PAYMOB_RAW_CALLBACK_KEY = "raw_callback"
        val PAYMOB_SUCCESS_KEYS = setOf("success", "issuccess", "is_success")
        val PAYMOB_PENDING_KEYS = setOf("pending", "ispending", "is_pending")
        val PAYMOB_ERROR_KEYS = setOf("erroroccured", "erroroccurred", "error_occured", "error_occurred")
    }
}
