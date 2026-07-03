package shopzen.domain.wishlist.repository

import kotlinx.coroutines.flow.Flow
import shopzen.domain.wishlist.model.WishlistItem

/**
 * Domain repository interface for the wishlist feature.
 */
interface WishlistRepository {
    fun getWishlist(userId: String): Flow<List<WishlistItem>>
    suspend fun addToWishlist(item: WishlistItem): Result<Unit>
    suspend fun removeFromWishlist(itemId: String): Result<Unit>
    fun isProductInWishlist(productId: String, userId: String): Flow<Boolean>
}
