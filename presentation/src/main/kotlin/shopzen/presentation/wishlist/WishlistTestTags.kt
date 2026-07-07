package shopzen.presentation.wishlist

internal object WishlistTestTags {
    const val Content = "wishlist_content"
    const val Header = "wishlist_header"
    const val Empty = "wishlist_empty"

    fun item(id: String) = "wishlist_item_$id"

    fun remove(id: String) = "wishlist_remove_$id"

    fun addToCart(id: String) = "wishlist_add_to_cart_$id"
}
