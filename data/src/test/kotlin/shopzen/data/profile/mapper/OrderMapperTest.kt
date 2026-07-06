package shopzen.data.profile.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import shopzen.data.profile.remote.dto.OrderDetailResponseDto
import shopzen.data.profile.remote.dto.OrderDiscountCodeDto
import shopzen.data.profile.remote.dto.OrderDto
import shopzen.data.profile.remote.dto.OrderLineItemDto
import shopzen.data.profile.remote.dto.OrderNoteAttributeDto

class OrderMapperTest {

    @Test
    fun `toDomain maps order fields and line items`() {
        val dto = OrderDto(
            id = 10L,
            name = "#1010",
            subtotalPrice = "120.00",
            totalPrice = "99.95",
            totalDiscounts = "20.05",
            currency = "EGP",
            paymentGatewayNames = listOf("manual"),
            discountCodes = listOf(OrderDiscountCodeDto(code = "SAVE20", amount = "20.05")),
            noteAttributes = listOf(OrderNoteAttributeDto(name = "payment_method", value = "CASH_ON_DELIVERY")),
            financialStatus = "paid",
            fulfillmentStatus = "fulfilled",
            createdAt = "2026-07-01T10:15:30Z",
            lineItems = listOf(
                OrderLineItemDto(
                    id = 1L,
                    title = "Black Shirt",
                    quantity = 2,
                    price = "49.50",
                    variantTitle = "M",
                )
            ),
        )

        val order = dto.toDomain()

        assertEquals("10", order.id)
        assertEquals("#1010", order.orderNumber)
        assertEquals(120.0, order.subtotalPrice, 0.0)
        assertEquals(20.05, order.discountAmount, 0.0)
        assertEquals("SAVE20", order.discountCode)
        assertEquals(99.95, order.totalPrice, 0.0)
        assertEquals("EGP", order.currency)
        assertEquals("CASH_ON_DELIVERY", order.paymentMethod)
        assertEquals("paid", order.financialStatus)
        assertEquals("fulfilled", order.fulfillmentStatus)
        assertEquals("2026-07-01T10:15:30Z", order.createdAt)
        assertEquals(1, order.lineItems.size)
        assertEquals("Black Shirt", order.lineItems.first().title)
        assertEquals(2, order.lineItems.first().quantity)
        assertEquals(49.50, order.lineItems.first().price, 0.0)
        assertEquals("M", order.lineItems.first().variantTitle)
    }

    @Test
    fun `toDomain uses safe defaults for missing optional values`() {
        val dto = OrderDto(
            id = 11L,
            name = null,
            orderNumber = 1011,
            totalPrice = "bad",
            currency = "",
            financialStatus = null,
            fulfillmentStatus = null,
            createdAt = "",
            lineItems = emptyList(),
        )

        val order = dto.toDomain()

        assertEquals("#1011", order.orderNumber)
        assertEquals(0.0, order.totalPrice, 0.0)
        assertEquals(0.0, order.subtotalPrice, 0.0)
        assertEquals(0.0, order.discountAmount, 0.0)
        assertEquals(null, order.discountCode)
        assertEquals("USD", order.currency)
        assertEquals("unknown", order.paymentMethod)
        assertEquals("unknown", order.financialStatus)
        assertEquals("unknown", order.fulfillmentStatus)
        assertTrue(order.lineItems.isEmpty())
    }

    @Test
    fun `detail response order maps line item price fallback`() {
        val response = OrderDetailResponseDto(
            order = OrderDto(
                id = 12L,
                name = "#1012",
                totalPrice = "150.00",
                currency = "USD",
                lineItems = listOf(
                    OrderLineItemDto(
                        id = 5L,
                        title = "Sneakers",
                        quantity = 3,
                        price = "bad",
                        variantTitle = null,
                    )
                ),
            )
        )

        val order = response.order.toDomain()

        assertEquals("#1012", order.orderNumber)
        assertEquals(1, order.lineItems.size)
        assertEquals(0.0, order.lineItems.first().price, 0.0)
        assertEquals(null, order.lineItems.first().variantTitle)
    }

    @Test
    fun `toDomain falls back to gateway payment method and discount code amounts`() {
        val dto = OrderDto(
            id = 13L,
            name = "#1013",
            totalPrice = "80.00",
            currency = "USD",
            gateway = "paymob",
            discountCodes = listOf(OrderDiscountCodeDto(code = "SAVE", amount = "15.00")),
        )

        val order = dto.toDomain()

        assertEquals(95.0, order.subtotalPrice, 0.0)
        assertEquals(15.0, order.discountAmount, 0.0)
        assertEquals("SAVE", order.discountCode)
        assertEquals("paymob", order.paymentMethod)
    }

    @Test
    fun `toDomain uses original line items subtotal when Shopify subtotal is already discounted`() {
        val dto = OrderDto(
            id = 14L,
            name = "#1014",
            subtotalPrice = "80.00",
            totalLineItemsPrice = "100.00",
            totalPrice = "80.00",
            totalDiscounts = "10.00",
            currency = "EGP",
            discountCodes = listOf(
                OrderDiscountCodeDto(code = "SUMMER20", amount = "10.00", type = "percentage")
            ),
        )

        val order = dto.toDomain()

        assertEquals(100.0, order.subtotalPrice, 0.0)
        assertEquals(20.0, order.discountAmount, 0.0)
        assertEquals(80.0, order.totalPrice, 0.0)
        assertEquals("SUMMER20", order.discountCode)
    }
}
