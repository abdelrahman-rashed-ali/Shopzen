package shopzen.data.checkout.mapper

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.DiscountCode
import shopzen.domain.cart.model.DiscountType
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.profile.model.Address

class CheckoutMapperTest {

    private val mapper = CheckoutMapper()

    @Test
    fun `toRestOrderCreateBody persists percentage coupon value and payment method`() {
        val body = mapper.toRestOrderCreateBody(sampleCheckout())
        val order = body["order"]!!.jsonObject
        val discountCode = order["discount_codes"]!!.jsonArray.first().jsonObject
        val paymentMethod = order["note_attributes"]!!.jsonArray.first().jsonObject

        assertEquals("SAVE10", discountCode["code"]!!.jsonPrimitive.content)
        assertEquals("10.0", discountCode["amount"]!!.jsonPrimitive.content)
        assertEquals("percentage", discountCode["type"]!!.jsonPrimitive.content)
        assertEquals("payment_method", paymentMethod["name"]!!.jsonPrimitive.content)
        assertEquals("ONLINE_PAYMENT", paymentMethod["value"]!!.jsonPrimitive.content)
    }

    @Test
    fun `toRestOrderCreateBody infers percentage coupon value when cart cache only has code`() {
        val body = mapper.toRestOrderCreateBody(
            sampleCheckout(
                subtotalPrice = 100.0,
                discountAmount = 20.0,
                totalPrice = 80.0,
                coupon = DiscountCode(
                    code = "SUMMER20",
                    discountType = DiscountType.PERCENTAGE,
                    value = 0.0,
                ),
            )
        )
        val discountCode = body["order"]!!
            .jsonObject["discount_codes"]!!
            .jsonArray.first()
            .jsonObject

        assertEquals("SUMMER20", discountCode["code"]!!.jsonPrimitive.content)
        assertEquals("20.0", discountCode["amount"]!!.jsonPrimitive.content)
        assertEquals("percentage", discountCode["type"]!!.jsonPrimitive.content)
    }

    private fun sampleCheckout(
        subtotalPrice: Double = 245.0,
        discountAmount: Double = 24.5,
        totalPrice: Double = 220.5,
        coupon: DiscountCode? = DiscountCode(
            code = "SAVE10",
            discountType = DiscountType.PERCENTAGE,
            value = 10.0,
        ),
    ) = Checkout(
        lineItems = listOf(
            CartItem(
                id = "line-1",
                productId = "product-1",
                variantId = "gid://shopify/ProductVariant/123",
                title = "Blue Hoodie",
                variantTitle = "M",
                price = 245.0,
                quantity = 1,
                maxQuantity = 3,
                imageUrl = "",
                userId = "user-1",
            )
        ),
        shippingAddress = Address(
            id = "address-1",
            recipientName = "Ada Lovelace",
            addressLine1 = "1 Main St",
            city = "Cairo",
            postalCode = "12345",
            country = "Egypt",
            phone = "+201000000000",
            isDefault = true,
        ),
        subtotalPrice = subtotalPrice,
        discountAmount = discountAmount,
        totalPrice = totalPrice,
        currency = "USD",
        appliedCoupon = coupon,
        selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
        customerId = 123L,
        customerEmail = "ada@example.com",
    )
}
