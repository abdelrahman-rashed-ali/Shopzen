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
    val showSizeRequiredError: Boolean = false,
    /** Currently selected currency from user preferences. */
    val currency: AppCurrency = AppCurrency.USD,
    /**
     * Pre-converted display price for the selected variant (or product default),
     * already multiplied by [currency.rateFromUsd]. Null until product is loaded.
     */
    val convertedPrice: Double? = null,
    val convertedCompareAtPrice: Double? = null,
)
