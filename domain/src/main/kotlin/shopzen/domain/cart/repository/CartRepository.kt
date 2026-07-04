package shopzen.domain.cart.repository

import kotlinx.coroutines.flow.Flow
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.CouponValidationResult

interface CartRepository {
    fun getCart(userId: String, forceRefresh: Boolean = false): Flow<Result<Cart>>
    suspend fun addToCart(item: CartItem): Result<Unit>
    suspend fun removeFromCart(itemId: String, userId: String): Result<Unit>
    suspend fun updateItemQuantity(itemId: String, quantity: Int, userId: String): Result<Unit>
    suspend fun clearCart(userId: String): Result<Unit>
    suspend fun applyCoupon(userId: String, code: String): Result<Unit>
    suspend fun removeCoupon(userId: String, code: String): Result<Unit>
}
