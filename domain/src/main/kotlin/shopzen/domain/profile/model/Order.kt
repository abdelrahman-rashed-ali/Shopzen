package shopzen.domain.profile.model

/**
 * A Shopify order shown in account order history.
 */
data class Order(
    val id: String,
    val orderNumber: String,
    val subtotalPrice: Double = 0.0,
    val discountAmount: Double = 0.0,
    val discountCode: String? = null,
    val totalPrice: Double,
    val currency: String,
    val paymentMethod: String = "",
    val financialStatus: String,
    val fulfillmentStatus: String,
    val createdAt: String,
    val lineItems: List<OrderLineItem>,
)
