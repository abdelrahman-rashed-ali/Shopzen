package shopzen.domain.checkout.repository

import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import shopzen.domain.checkout.model.PaymobPaymentIntention

interface CheckoutRepository {
    suspend fun placeOrder(checkout: Checkout): Result<OrderConfirmation>

    /** Creates a Paymob payment intention for the checkout online payment flow. */
    suspend fun createPaymobPaymentIntention(checkout: Checkout): Result<PaymobPaymentIntention>
}
