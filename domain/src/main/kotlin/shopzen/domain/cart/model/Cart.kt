package shopzen.domain.cart.model

data class Cart(
    val items: List<CartItem>,
    val currency: String,
    val subtotalPrice: Double,
    val userId: String,
)
