package shopzen.data.wishlist.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import shopzen.data.wishlist.local.entity.WishlistEntity

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWishlist(userId: String): Flow<List<WishlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWishlistItem(item: WishlistEntity): Long

    @Query("DELETE FROM wishlist_items WHERE id = :itemId")
    fun deleteWishlistItem(itemId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId AND userId = :userId)")
    fun isProductInWishlist(productId: String, userId: String): Flow<Boolean>
}
