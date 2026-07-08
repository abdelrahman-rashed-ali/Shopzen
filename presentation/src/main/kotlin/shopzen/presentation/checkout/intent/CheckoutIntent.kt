package shopzen.presentation.checkout.intent

import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.checkout.model.PaymobIntentionId

sealed class CheckoutIntent {
    data object LoadCheckout : CheckoutIntent()
    data class SelectShippingAddress(val addressId: String) : CheckoutIntent()
    data object AddAddressClicked : CheckoutIntent()
    data object ContinueToPayment : CheckoutIntent()
    data class SelectPaymentMethod(val method: PaymentMethod) : CheckoutIntent()
    data object RequestPlaceOrder : CheckoutIntent()
    data object ConfirmPlaceOrder : CheckoutIntent()
    data class ConsumePendingPaymobLaunch(val intentionId: PaymobIntentionId) : CheckoutIntent()
    data class OnlinePaymentSucceeded(val payResponse: Map<String, String?>) : CheckoutIntent()
    data class OnlinePaymentFailed(val message: String?) : CheckoutIntent()
    data object OnlinePaymentPending : CheckoutIntent()
    data object DismissPlaceOrderDialog : CheckoutIntent()
    data object Retry : CheckoutIntent()
    data object DismissError : CheckoutIntent()
}
