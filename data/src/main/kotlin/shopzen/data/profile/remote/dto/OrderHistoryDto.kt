package shopzen.data.profile.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderHistoryResponseDto(
    val orders: List<OrderDto> = emptyList(),
)

@Serializable
data class OrderDetailResponseDto(
    val order: OrderDto,
)

@Serializable
data class OrderDto(
    val id: Long,
    val name: String? = null,
    @SerialName("order_number") val orderNumber: Int? = null,
    @SerialName("subtotal_price") val subtotalPrice: String? = null,
    @SerialName("total_line_items_price") val totalLineItemsPrice: String? = null,
    @SerialName("total_price") val totalPrice: String = "0.00",
    @SerialName("total_discounts") val totalDiscounts: String? = null,
    @SerialName("current_total_discounts") val currentTotalDiscounts: String? = null,
    val currency: String = "",
    val gateway: String? = null,
    @SerialName("payment_gateway_names") val paymentGatewayNames: List<String> = emptyList(),
    @SerialName("discount_codes") val discountCodes: List<OrderDiscountCodeDto> = emptyList(),
    @SerialName("note_attributes") val noteAttributes: List<OrderNoteAttributeDto> = emptyList(),
    @SerialName("financial_status") val financialStatus: String? = null,
    @SerialName("fulfillment_status") val fulfillmentStatus: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("line_items") val lineItems: List<OrderLineItemDto> = emptyList(),
)

@Serializable
data class OrderLineItemDto(
    val id: Long,
    val title: String = "",
    val quantity: Int = 0,
    val price: String = "0.00",
    @SerialName("variant_title") val variantTitle: String? = null,
)

@Serializable
data class OrderDiscountCodeDto(
    val code: String = "",
    val amount: String = "0.00",
    val type: String = "",
)

@Serializable
data class OrderNoteAttributeDto(
    val name: String = "",
    val value: String = "",
)
