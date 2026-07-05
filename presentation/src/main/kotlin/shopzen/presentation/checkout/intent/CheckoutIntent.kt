package shopzen.presentation.checkout.intent

import shopzen.domain.checkout.model.PaymentMethod

sealed class CheckoutIntent {
    data object LoadCheckout : CheckoutIntent()
    data class SelectShippingAddress(val addressId: String) : CheckoutIntent()
    data object AddAddressClicked : CheckoutIntent()
    data object ContinueToPayment : CheckoutIntent()
    data class SelectPaymentMethod(val method: PaymentMethod) : CheckoutIntent()
    data object RequestPlaceOrder : CheckoutIntent()
    data object ConfirmPlaceOrder : CheckoutIntent()
    data object DismissPlaceOrderDialog : CheckoutIntent()
    data object Retry : CheckoutIntent()
    data object DismissError : CheckoutIntent()
}
