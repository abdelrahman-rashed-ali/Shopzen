package shopzen.presentation.product.intent

/**
 * MVI sealed class — every user or system event on the Product Detail screen.
 */
sealed class ProductDetailIntent {
    data class LoadProduct(val productId: Long) : ProductDetailIntent()
    data class SelectVariant(val variantId: Long) : ProductDetailIntent()
    data object RequireVariantSelection : ProductDetailIntent()
    data object ToggleWishlist : ProductDetailIntent()
    data object Retry : ProductDetailIntent()
}
