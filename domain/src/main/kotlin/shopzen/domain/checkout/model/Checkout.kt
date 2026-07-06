package shopzen.domain.checkout.model

import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.DiscountCode
import shopzen.domain.profile.model.Address

data class Checkout(
    val lineItems: List<CartItem>,
    val shippingAddress: Address,
    val subtotalPrice: Double,
    val discountAmount: Double,
    val totalPrice: Double,
    val currency: String,
    val appliedCoupon: DiscountCode?,
    val selectedPaymentMethod: PaymentMethod?,
    val customerId: Long? = null,
    val customerEmail: String = "",
)
