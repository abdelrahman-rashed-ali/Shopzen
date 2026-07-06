package shopzen.presentation.wishlist.intent

sealed class WishlistIntent {
    object LoadWishlist : WishlistIntent()
    data class RequestRemoveItem(val itemId: String) : WishlistIntent()
    data class ConfirmRemoveItem(val itemId: String) : WishlistIntent()
    object DismissConfirmDialog : WishlistIntent()
    data class OpenProduct(val productId: String) : WishlistIntent()
    data class RequestAddToWishlist(val product: shopzen.domain.catalog.model.Product) : WishlistIntent()
    data class ConfirmAddToWishlist(val product: shopzen.domain.catalog.model.Product) : WishlistIntent()
    object DismissAddConfirmDialog : WishlistIntent()
    data class AddToWishlist(val product: shopzen.domain.catalog.model.Product) : WishlistIntent()
    data class AddToCart(val productId: String) : WishlistIntent()
    object DismissLoginRequiredDialog : WishlistIntent()
}
