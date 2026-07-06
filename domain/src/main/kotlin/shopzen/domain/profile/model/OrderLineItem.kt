package shopzen.domain.profile.model

/**
 * A product line included in a past Shopify order.
 */
data class OrderLineItem(
    val id: String,
    val title: String,
    val quantity: Int,
    val price: Double,
    val variantTitle: String?,
)
