package shopzen.data.profile.mapper

import shopzen.data.profile.remote.dto.OrderDto
import shopzen.data.profile.remote.dto.OrderLineItemDto
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.model.OrderLineItem

fun OrderDto.toDomain(): Order {
    val rawTotal = totalPrice.toDoubleOrNull() ?: 0.0
    val fieldDiscount = resolvedDiscountAmount()
    val resolvedSubtotal = resolvedSubtotalPrice(rawTotal = rawTotal, fieldDiscount = fieldDiscount)
    val resolvedDiscount = maxOf(fieldDiscount, (resolvedSubtotal - rawTotal).coerceAtLeast(0.0))
    val resolvedTotal = (resolvedSubtotal - resolvedDiscount).coerceAtLeast(0.0)

    return Order(
        id = id.toString(),
        orderNumber = name?.takeIf { it.isNotBlank() }
            ?: orderNumber?.let { "#$it" }
            ?: id.toString(),
        subtotalPrice = resolvedSubtotal,
        discountAmount = resolvedDiscount,
        discountCode = discountCodes.firstNotNullOfOrNull { it.code.takeIf(String::isNotBlank) },
        totalPrice = resolvedTotal,
        currency = currency.ifBlank { DEFAULT_CURRENCY },
        paymentMethod = resolvedPaymentMethod(),
        financialStatus = financialStatus.orUnknownStatus(),
        fulfillmentStatus = fulfillmentStatus.orUnknownStatus(),
        createdAt = createdAt,
        lineItems = lineItems.map { it.toDomain() },
    )
}

fun OrderLineItemDto.toDomain(): OrderLineItem =
    OrderLineItem(
        id = id.toString(),
        title = title,
        quantity = quantity,
        price = price.toDoubleOrNull() ?: 0.0,
        variantTitle = variantTitle,
    )

private fun String?.orUnknownStatus(): String =
    this?.takeIf { it.isNotBlank() } ?: UNKNOWN_STATUS

private fun OrderDto.resolvedDiscountAmount(): Double =
    currentTotalDiscounts?.toDoubleOrNull()
        ?: totalDiscounts?.toDoubleOrNull()
        ?: discountCodes.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

private fun OrderDto.resolvedSubtotalPrice(
    rawTotal: Double,
    fieldDiscount: Double,
): Double =
    totalLineItemsPrice?.toDoubleOrNull()
        ?: subtotalPrice?.toDoubleOrNull()?.let { subtotal ->
            if (fieldDiscount > 0.0 && subtotal <= rawTotal) {
                rawTotal + fieldDiscount
            } else {
                subtotal
            }
        }
        ?: (rawTotal + fieldDiscount)

private fun OrderDto.resolvedPaymentMethod(): String =
    noteAttributes.firstNotNullOfOrNull { attribute ->
        attribute.value.takeIf {
            attribute.name.equals(PAYMENT_METHOD_NOTE_KEY, ignoreCase = true) && it.isNotBlank()
        }
    }
        ?: paymentGatewayNames.firstOrNull { it.isNotBlank() }
        ?: gateway?.takeIf { it.isNotBlank() }
        ?: UNKNOWN_STATUS

private const val DEFAULT_CURRENCY = "USD"
private const val UNKNOWN_STATUS = "unknown"
private const val PAYMENT_METHOD_NOTE_KEY = "payment_method"
