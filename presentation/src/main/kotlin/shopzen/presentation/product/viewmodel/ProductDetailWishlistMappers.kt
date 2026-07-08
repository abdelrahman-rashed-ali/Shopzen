package shopzen.presentation.product.viewmodel

import java.util.UUID
import shopzen.domain.product.model.Product
import shopzen.domain.wishlist.model.WishlistItem
import shopzen.presentation.product.state.ProductDetailState

internal fun Product.initialVariantId(): Long? =
    if (variants.size == 1) variants.first().id else null

internal fun List<WishlistItem>.findProductWishlistItem(productId: Long): WishlistItem? =
    firstOrNull { it.productId == productId.toString() }

internal suspend fun ProductDetailState.toggleWishlistResult(
    product: Product,
    userId: String,
    addToWishlist: suspend (WishlistItem) -> Result<Unit>,
    removeFromWishlist: suspend (String) -> Result<Unit>,
): Result<Unit> =
    if (isWishlisted) {
        wishlistItemId?.let { removeFromWishlist(it) }
            ?: Result.failure(IllegalStateException("Wishlist item is unavailable"))
    } else {
        addToWishlist(product.toWishlistItem(userId, selectedVariantId))
    }

internal fun Product.toWishlistItem(userId: String, selectedVariantId: Long?): WishlistItem {
    val selectedVariant = selectedVariantId?.let { variantId ->
        variants.firstOrNull { it.id == variantId }
    }

    return WishlistItem(
        id = UUID.randomUUID().toString(),
        productId = id.toString(),
        title = title,
        vendor = vendor,
        price = selectedVariant?.price ?: price,
        imageUrl = images.firstOrNull()?.src.orEmpty(),
        userId = userId,
        addedAt = System.currentTimeMillis(),
    )
}
