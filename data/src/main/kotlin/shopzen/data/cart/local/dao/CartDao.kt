package shopzen.data.cart.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import shopzen.data.cart.local.entity.CartItemEntity

@Dao
interface CartDao {

    /** Reactive stream of all cart items for [userId]. Emits on every DB mutation. */
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun observeByUser(userId: String): Flow<List<CartItemEntity>>

    /** Returns all items for [userId] as a one-shot snapshot (used for expiry checks). */
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    suspend fun getByUser(userId: String): List<CartItemEntity>

    /** Inserts or replaces rows. Used after every remote sync. */
    @Upsert
    suspend fun upsertAll(items: List<CartItemEntity>)

    /** Deletes a single line item. Called on remove-from-cart. */
    @Query("DELETE FROM cart_items WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: String, userId: String)

    /** Clears the entire cart for [userId]. Called on checkout completion or explicit clear. */
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}
