package iti.domain.catalog.model

/**
 * Represents a product category (Shopify custom collection).
 *
 * @property id Unique category identifier.
 * @property title Display name of the category.
 * @property imageUrl Optional category image URL.
 */
data class Category(
    val id: String,
    val title: String,
    val imageUrl: String? = null
)
