package shopzen.presentation.product.viewmodel

import shopzen.domain.product.model.Product
import shopzen.domain.wishlist.model.WishlistItem
import shopzen.presentation.product.state.ProductDetailState

internal fun ProductDetailState.loadingProduct(): ProductDetailState =
    copy(isLoading = true, error = null)

internal fun ProductDetailState.withLoadedProduct(product: Product): ProductDetailState =
    copy(
        isLoading = false,
        product = product,
        selectedVariantId = product.initialVariantId(),
        error = null,
    )

internal fun ProductDetailState.withProductLoadError(message: String?): ProductDetailState =
    copy(isLoading = false, error = message ?: "Failed to load product")

internal fun ProductDetailState.withWishlistItem(item: WishlistItem?): ProductDetailState =
    copy(
        isWishlisted = item != null,
        wishlistItemId = item?.id,
        isWishlistUpdating = false,
    )

internal fun ProductDetailState.withSelectedVariant(variantId: Long): ProductDetailState =
    copy(selectedVariantId = variantId, showVariantRequiredError = false)

internal fun ProductDetailState.withVariantRequiredError(): ProductDetailState =
    copy(showVariantRequiredError = true)

internal fun ProductDetailState.withWishlistUpdating(isUpdating: Boolean): ProductDetailState =
    copy(isWishlistUpdating = isUpdating)
