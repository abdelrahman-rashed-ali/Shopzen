package shopzen.presentation.checkout.state

import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.checkout.model.PaymobClientSecret
import shopzen.domain.checkout.model.PaymobIntentionId
import shopzen.domain.checkout.model.PaymobPublicKey
import shopzen.domain.profile.model.Address
import shopzen.presentation.common.util.UiText

data class CheckoutItemUi(
    val id: String,
    val title: String,
    val variantTitle: String,
    val formattedUnitPrice: String,
    val quantity: Int,
    val formattedLineTotal: String,
    val imageUrl: String,
)

data class PendingPaymobLaunch(
    val intentionId: PaymobIntentionId,
    val clientSecret: PaymobClientSecret,
    val publicKey: PaymobPublicKey,
)

data class CheckoutState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val items: List<CheckoutItemUi> = emptyList(),
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null,
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val formattedSubtotal: String = "",
    val formattedDiscount: String? = null,
    val formattedTotal: String = "",
    val appliedCouponLabel: String? = null,
    val isPlacingOrder: Boolean = false,
    val isOnlinePaymentInProgress: Boolean = false,
    val pendingPaymobLaunch: PendingPaymobLaunch? = null,
    val showPlaceOrderDialog: Boolean = false,
) {
    val selectedAddress: Address?
        get() = addresses.firstOrNull { it.id == selectedAddressId }

    val canContinueToPayment: Boolean
        get() = items.isNotEmpty() && selectedAddress != null

    val canPlaceOrder: Boolean
        get() = canContinueToPayment &&
            selectedPaymentMethod != null &&
            !isPlacingOrder &&
            !isOnlinePaymentInProgress
}
