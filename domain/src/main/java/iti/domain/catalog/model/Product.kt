package iti.domain.catalog.model

/**
 * Represents a product in the Shopzen catalog.
 *
 * @property id Unique product identifier from Shopify.
 * @property title Display name of the product.
 * @property vendor Brand/vendor name.
 * @property productType Category type of the product.
 * @property price Formatted price string of the default variant.
 * @property imageUrl Primary product image URL.
 * @property images All product image URLs.
 * @property createdAt ISO 8601 timestamp of when the product was created.
 */
data class Product(
    val id: String,
    val title: String,
    val vendor: String,
    val productType: String,
    val price: String,
    val imageUrl: String,
    val images: List<String> = emptyList(),
    val createdAt: String = ""
)
