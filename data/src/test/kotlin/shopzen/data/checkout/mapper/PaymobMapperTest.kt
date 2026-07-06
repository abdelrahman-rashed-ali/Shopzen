package shopzen.data.checkout.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import shopzen.data.remote.config.PaymobConfig
import shopzen.domain.cart.model.CartItem
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.profile.model.Address

class PaymobMapperTest {

    private val mapper = PaymobMapper()

    @Test
    fun `toPaymentIntentionRequest converts USD total to EGP cents`() {
        val request = mapper.toPaymentIntentionRequest(
            checkout = sampleCheckout(totalPrice = 45.0, currency = "USD"),
            config = paymobConfig(),
            specialReference = "ref-1",
            exchangeRate = 50.0,
        )

        assertEquals("EGP", request.currency)
        assertEquals(225_000, request.amount)
        assertEquals(225_000, request.items.single().amount)
    }

    @Test
    fun `toPaymentIntentionRequest converts EUR and GBP totals to EGP cents`() {
        val eurRequest = mapper.toPaymentIntentionRequest(
            checkout = sampleCheckout(totalPrice = 10.0, currency = "EUR"),
            config = paymobConfig(),
            specialReference = "ref-1",
            exchangeRate = 60.0,
        )
        val gbpRequest = mapper.toPaymentIntentionRequest(
            checkout = sampleCheckout(totalPrice = 10.0, currency = "GBP"),
            config = paymobConfig(),
            specialReference = "ref-2",
            exchangeRate = 70.0,
        )

        assertEquals(60_000, eurRequest.amount)
        assertEquals(70_000, gbpRequest.amount)
    }

    @Test
    fun `toPaymentIntentionRequest keeps EGP totals unchanged`() {
        val request = mapper.toPaymentIntentionRequest(
            checkout = sampleCheckout(totalPrice = 45.0, currency = "EGP"),
            config = paymobConfig(),
            specialReference = "ref-1",
            exchangeRate = 1.0,
        )

        assertEquals(4_500, request.amount)
    }

    @Test
    fun `toPaymentIntentionRequest fails when EGP exchange rate is missing`() {
        val error = assertThrows(IllegalStateException::class.java) {
            mapper.toPaymentIntentionRequest(
                checkout = sampleCheckout(totalPrice = 45.0, currency = "USD"),
                config = paymobConfig(),
                specialReference = "ref-1",
                exchangeRate = 0.0,
            )
        }

        assertEquals("Missing exchange rate for USD to EGP", error.message)
    }

    private fun paymobConfig() = PaymobConfig(
        baseUrl = "https://accept.paymob.com",
        publicKey = "public",
        secretKey = "secret",
        currency = "EGP",
        onlineCardIntegrationId = 123,
    )

    private fun sampleCheckout(
        totalPrice: Double,
        currency: String,
    ) = Checkout(
        lineItems = listOf(
            CartItem(
                id = "line-1",
                productId = "product-1",
                variantId = "variant-1",
                title = "Blue Hoodie",
                variantTitle = "Size M",
                price = totalPrice,
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
        subtotalPrice = totalPrice,
        discountAmount = 0.0,
        totalPrice = totalPrice,
        currency = currency,
        appliedCoupon = null,
        selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
        customerId = 123L,
        customerEmail = "ada@example.com",
    )
}
