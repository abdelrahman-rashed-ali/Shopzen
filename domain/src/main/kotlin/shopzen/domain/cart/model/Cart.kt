package shopzen.domain.cart.model

data class Cart(
    val items: List<CartItem>,
    val currency: String,
    val subtotalPrice: Double,
    val discountAmount: Double = 0.0,
    val totalPrice: Double = subtotalPrice,
    val appliedCoupon: DiscountCode? = null,
    val userId: String,
)
