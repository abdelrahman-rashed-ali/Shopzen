package shopzen.domain.checkout.repository

import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation

interface CheckoutRepository {
    suspend fun placeOrder(checkout: Checkout): Result<OrderConfirmation>
}
