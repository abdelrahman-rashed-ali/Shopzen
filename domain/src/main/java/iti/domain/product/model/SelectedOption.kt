package iti.domain.product.model

/**
 * A name-value pair representing a selected option on a variant
 * (e.g. name="Size", value="M").
 */
data class SelectedOption(
    val name: String,
    val value: String,
)
