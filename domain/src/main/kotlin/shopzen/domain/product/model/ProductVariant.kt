package shopzen.domain.product.model

/**
 * A product variant (e.g. "Medium / Red").
 * [inventoryQuantity] is used for stock enforcement —
 * variants with 0 quantity are rendered as disabled in the UI.
 */
data class ProductVariant(
    val id: Long,
    val title: String,
    val price: String,
    val compareAtPrice: String?,
    val inventoryQuantity: Int,
    val selectedOptions: List<SelectedOption>,
)
