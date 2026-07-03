package shopzen.data.wishlist.local

import kotlinx.coroutines.flow.Flow
import shopzen.data.wishlist.local.dao.WishlistDao
import shopzen.data.wishlist.local.entity.WishlistEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalWishlistDataSource @Inject constructor(
    private val wishlistDao: WishlistDao
) {
    fun getWishlist(userId: String): Flow<List<WishlistEntity>> {
        return wishlistDao.getWishlist(userId)
    }

    suspend fun insertWishlistItem(item: WishlistEntity) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        wishlistDao.insertWishlistItem(item)
    }

    suspend fun deleteWishlistItem(itemId: String) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        wishlistDao.deleteWishlistItem(itemId)
    }

    fun isProductInWishlist(productId: String, userId: String): Flow<Boolean> {
        return wishlistDao.isProductInWishlist(productId, userId)
    }
}
