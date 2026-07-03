package shopzen.domain.wishlist.model

/**
 * Pure Kotlin domain model representing a saved wishlist item.
 */
data class WishlistItem(
    val id: String,
    val productId: String,
    val title: String,
    val vendor: String,
    val price: String,
    val imageUrl: String,
    val userId: String,
    val addedAt: Long
)
