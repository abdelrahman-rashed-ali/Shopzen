package iti.domain.catalog.model

/**
 * Represents a brand/vendor in the catalog.
 *
 * @property name Display name of the brand.
 * @property imageUrl Optional brand logo or representative image URL.
 */
data class Brand(
    val name: String,
    val imageUrl: String? = null
)
