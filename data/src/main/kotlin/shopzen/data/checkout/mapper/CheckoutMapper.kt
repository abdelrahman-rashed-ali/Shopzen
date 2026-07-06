package shopzen.data.checkout.mapper

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import shopzen.data.checkout.remote.dto.OrderDto
import shopzen.domain.cart.model.DiscountCode
import shopzen.domain.cart.model.DiscountType
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import javax.inject.Inject

class CheckoutMapper @Inject constructor() {
    fun toRestOrderCreateBody(checkout: Checkout): JsonObject =
        buildJsonObject {
            putJsonObject("order") {
                put("line_items", buildJsonArray {
                    checkout.lineItems.forEach { item ->
                        addJsonObject {
                            put("variant_id", item.variantId.toShopifyRestId())
                            put("quantity", item.quantity)
                        }
                    }
                })
                put("financial_status", "pending")
                put("send_receipt", true)
                put("inventory_behaviour", "decrement_obeying_policy")
                checkout.customerId?.let { customerId ->
                    putJsonObject("customer") {
                        put("id", customerId)
                    }
                }
                if (checkout.customerEmail.isNotBlank()) {
                    put("email", checkout.customerEmail)
                }
                put("shipping_address", toRestShippingAddressJson(checkout))
                checkout.selectedPaymentMethod?.let { paymentMethod ->
                    put("note_attributes", buildJsonArray {
                        addJsonObject {
                            put("name", PAYMENT_METHOD_NOTE_KEY)
                            put("value", paymentMethod.name)
                        }
                    })
                }
                checkout.appliedCoupon?.let { coupon ->
                    put("discount_codes", buildJsonArray {
                        add(toRestDiscountCodeJson(coupon, checkout))
                    })
                }
            }
        }

    fun toOrderCreateVariables(checkout: Checkout): JsonObject =
        buildJsonObject {
            putJsonObject("order") {
                if (checkout.currency.isNotBlank()) {
                    put("currency", checkout.currency)
                }
                checkout.customerId?.let { customerId ->
                    putJsonObject("customer") {
                        put("id", "gid://shopify/Customer/$customerId")
                    }
                }
                if (checkout.customerEmail.isNotBlank()) {
                    put("email", checkout.customerEmail)
                }
                put("lineItems", buildJsonArray {
                    checkout.lineItems.forEach { item ->
                        addJsonObject {
                            put("variantId", toVariantGid(item.variantId))
                            put("quantity", item.quantity)
                        }
                    }
                })
                checkout.shippingAddress.phone?.let { put("phone", it) }
                put("shippingAddress", toMailingAddressJson(checkout))
                checkout.appliedCoupon?.let { coupon ->
                    put("discountCode", toDiscountCodeJson(coupon, checkout.currency))
                }
            }
            putJsonObject("options") {
                put("sendReceipt", true)
            }
        }

    fun toOrderConfirmation(order: OrderDto): OrderConfirmation =
        OrderConfirmation(
            orderId = order.id,
            orderNumber = order.name,
        )

    private fun toRestShippingAddressJson(checkout: Checkout): JsonObject {
        val nameParts = checkout.shippingAddress.recipientName.trim().split(Regex("\\s+"))
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")

        return buildJsonObject {
            if (firstName.isNotBlank()) put("first_name", firstName)
            if (lastName.isNotBlank()) put("last_name", lastName)
            checkout.shippingAddress.phone?.let { put("phone", it) }
            put("address1", checkout.shippingAddress.addressLine1)
            checkout.shippingAddress.addressLine2?.let { put("address2", it) }
            put("city", checkout.shippingAddress.city)
            checkout.shippingAddress.stateOrProvince?.let { put("province", it) }
            put("country", checkout.shippingAddress.country)
            put("zip", checkout.shippingAddress.postalCode)
        }
    }

    private fun toRestDiscountCodeJson(
        coupon: DiscountCode,
        checkout: Checkout,
    ): JsonObject =
        buildJsonObject {
            put("code", coupon.code)
            put(
                "amount",
                when (coupon.discountType) {
                    DiscountType.PERCENTAGE -> coupon.effectivePercentage(checkout)
                    DiscountType.FIXED_AMOUNT -> coupon.value
                }.toString(),
            )
            put(
                "type",
                when (coupon.discountType) {
                    DiscountType.PERCENTAGE -> "percentage"
                    DiscountType.FIXED_AMOUNT -> "fixed_amount"
                },
            )
        }

    private fun DiscountCode.effectivePercentage(checkout: Checkout): Double =
        value.takeIf { it > 0.0 }
            ?: checkout.discountAmount
                .takeIf { checkout.subtotalPrice > 0.0 && it > 0.0 }
                ?.let { discount -> discount * 100.0 / checkout.subtotalPrice }
            ?: 0.0

    private fun toMailingAddressJson(checkout: Checkout): JsonObject {
        val nameParts = checkout.shippingAddress.recipientName.trim().split(Regex("\\s+"))
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.drop(1).joinToString(" ")

        return buildJsonObject {
            if (firstName.isNotBlank()) put("firstName", firstName)
            if (lastName.isNotBlank()) put("lastName", lastName)
            checkout.shippingAddress.phone?.let { put("phone", it) }
            put("address1", checkout.shippingAddress.addressLine1)
            checkout.shippingAddress.addressLine2?.let { put("address2", it) }
            put("city", checkout.shippingAddress.city)
            checkout.shippingAddress.stateOrProvince?.let { put("province", it) }
            put("country", checkout.shippingAddress.country)
            put("zip", checkout.shippingAddress.postalCode)
        }
    }

    private fun toDiscountCodeJson(coupon: DiscountCode, currency: String): JsonObject =
        buildJsonObject {
            when (coupon.discountType) {
                DiscountType.PERCENTAGE -> putJsonObject("itemPercentageDiscountCode") {
                    put("code", coupon.code)
                    put("percentage", coupon.value)
                }

                DiscountType.FIXED_AMOUNT -> putJsonObject("itemFixedDiscountCode") {
                    put("code", coupon.code)
                    putJsonObject("amountSet") {
                        putJsonObject("shopMoney") {
                            put("amount", coupon.value.toString())
                            put("currencyCode", currency)
                        }
                    }
                }
            }
        }

    private fun toVariantGid(variantId: String): String =
        if (variantId.startsWith("gid://")) variantId else "gid://shopify/ProductVariant/$variantId"

    private fun String.toShopifyRestId(): String =
        substringAfterLast("/")

    private companion object {
        const val PAYMENT_METHOD_NOTE_KEY = "payment_method"
    }
}
