package shopzen.domain.product.model

/**
 * Core business model representing a Shopify product.
 * Pure Kotlin — no Android or framework dependencies.
 */
data class Product(
    val id: Long,
    val title: String,
    val description: String,
    val vendor: String,
    val productType: String,
    val tags: List<String>,
    val images: List<ProductImage>,
    val variants: List<ProductVariant>,
    val options: List<ProductOption>,
    val price: String,
    val compareAtPrice: String?,
)
