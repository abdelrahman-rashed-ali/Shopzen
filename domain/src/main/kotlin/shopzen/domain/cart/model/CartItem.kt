package shopzen.domain.cart.model

data class CartItem(
    val id: String,
    val productId: String,
    val variantId: String,
    val title: String,
    val variantTitle: String,
    val price: Double,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String,
    val userId: String,
)
