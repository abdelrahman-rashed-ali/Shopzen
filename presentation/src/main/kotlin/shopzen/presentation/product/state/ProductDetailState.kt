package shopzen.presentation.product.state

import shopzen.domain.product.model.Product
import shopzen.domain.profile.model.AppCurrency

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
    /** Currently selected currency from user preferences. */
    val currency: AppCurrency = AppCurrency.USD,
    /**
     * Pre-converted display price for the selected variant (or product default),
     * already multiplied by [currency.rateFromUsd]. Null until product is loaded.
     */
    val convertedPrice: Double? = null,
    val convertedCompareAtPrice: Double? = null,
)

data class ProductReviewUi(
    val id: String,
    val authorName: String,
    val rating: Int,
    val body: String,
)
