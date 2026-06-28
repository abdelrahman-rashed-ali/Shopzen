package com.iti.myapplication.ui.product.state

import iti.domain.product.model.Product

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
)
