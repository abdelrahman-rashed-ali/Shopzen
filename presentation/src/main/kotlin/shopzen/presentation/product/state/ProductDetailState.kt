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

    /**
     * Preferred generic variant-selection error flag.
     *
     * Use this for product variants because not every product option is necessarily a size.
     */
    val showVariantRequiredError: Boolean = false,

    /**
     * Backward-compatible flag for flows that still refer to size selection.
     *
     * Keep this while resolving the remaining conflicts. Once all usages are migrated,
     * prefer [showVariantRequiredError].
     */
    val showSizeRequiredError: Boolean = false,

    val isWishlisted: Boolean = false,
    val wishlistItemId: String? = null,
    val isWishlistUpdating: Boolean = false,

    /** Currently selected currency from user preferences. */
    val currency: AppCurrency = AppCurrency.USD,

    /**
     * Pre-converted display price for the selected variant, or product default.
     * Already multiplied by [currency.rateFromUsd]. Null until product is loaded.
     */
    val convertedPrice: Double? = null,
    val convertedCompareAtPrice: Double? = null,

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