package shopzen.data.cart.local

import kotlinx.coroutines.flow.Flow
import shopzen.data.cart.local.dao.CartDao
import shopzen.data.cart.local.entity.CartItemEntity
import javax.inject.Inject

/**
 * Thin wrapper around [CartDao].
 * Repository accesses local cart data exclusively through this class — never injects [CartDao] directly.
 */
class CartLocalDataSource @Inject constructor(
    private val dao: CartDao,
) {
    fun observeCart(userId: String): Flow<List<CartItemEntity>> =
        dao.observeByUser(userId)

    suspend fun getCart(userId: String): List<CartItemEntity> =
        dao.getByUser(userId)

    suspend fun upsertAll(items: List<CartItemEntity>) =
        dao.upsertAll(items)

    suspend fun deleteItem(id: String, userId: String) =
        dao.deleteById(id, userId)

    suspend fun clearCart(userId: String) =
        dao.clearForUser(userId)
}
