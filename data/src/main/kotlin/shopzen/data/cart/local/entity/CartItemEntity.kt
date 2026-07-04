package shopzen.data.cart.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-persisted snapshot of a Shopify cart line item.
 *
 * [cartId]          — Shopify cart GID (`gid://shopify/Cart/...`); used to refresh from Storefront API.
 * [invalidationDate] — epoch millis when this cache entry expires.
 *                      Repository compares against [System.currentTimeMillis]; if expired, a remote
 *                      fetch is triggered and the result is upserted back into Room.
 *                      Default TTL: 5 minutes (see [shopzen.data.cart.repository.CartRepositoryImpl.CACHE_TTL_MS]).
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,             // Shopify CartLine GID
    val cartId: String,                     // Parent cart GID — needed for Storefront refresh
    val productId: String,
    val variantId: String,
    val title: String,
    val variantTitle: String,
    val price: Double,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String,
    val userId: String,
    val currency: String,
    val subtotalPrice: Double,
    val totalPrice: Double,
    val appliedCouponCode: String?,
    val appliedCouponApplicable: Boolean?,
    val invalidationDate: Long,             // epoch ms — cache expiry timestamp
)
