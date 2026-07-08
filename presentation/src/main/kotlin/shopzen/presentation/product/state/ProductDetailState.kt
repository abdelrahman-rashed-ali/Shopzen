package shopzen.presentation.product.state

import shopzen.domain.product.model.Product

/**
 * MVI ViewState for the Product Detail screen.
 * All fields have defaults per AGENTS.md convention.
 */
data class ProductDetailState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val product: Product? = null,
    val selectedVariantId: Long? = null,
    val showVariantRequiredError: Boolean = false,
    val isWishlisted: Boolean = false,
    val wishlistItemId: String? = null,
    val isWishlistUpdating: Boolean = false,
    val isReviewsLoading: Boolean = false,
    val reviews: List<ProductReviewUi> = emptyList(),
    val reviewsError: String? = null,
)

data class ProductReviewUi(
    val id: String,
    val authorName: String,
    val rating: Int,
    val body: String,
)
