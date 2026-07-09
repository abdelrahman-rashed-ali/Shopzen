package shopzen.domain.product.model

data class ProductComparisonData(
    val productA: ProductComparisonItem,
    val productB: ProductComparisonItem,
)

data class ProductComparisonItem(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val currency: String,
    val imageUrl: String?,
    val available: Boolean,
    val tags: List<String>,
    val variants: List<ProductVariantComparisonItem>,
    val rating: Double?,
    val reviewCount: Int?,
)

data class ProductVariantComparisonItem(
    val id: String,
    val title: String,
    val price: Double,
    val available: Boolean,
)
