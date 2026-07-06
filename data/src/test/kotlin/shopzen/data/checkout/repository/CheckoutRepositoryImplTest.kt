package shopzen.data.checkout.repository

import android.util.Log
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import shopzen.data.checkout.mapper.CheckoutMapper
import shopzen.data.checkout.mapper.PaymobMapper
import shopzen.data.checkout.remote.CheckoutRemoteDataSource
import shopzen.data.checkout.remote.ExchangeRateRemoteDataSource
import shopzen.data.checkout.remote.IdempotencyKey
import shopzen.data.checkout.remote.PaymobRemoteDataSource
import shopzen.data.checkout.remote.dto.OrderDto
import shopzen.data.checkout.remote.dto.OrderCreateRestResponse
import shopzen.data.checkout.remote.dto.PaymobPaymentIntentionRequest
import shopzen.data.checkout.remote.dto.PaymobPaymentIntentionResponse
import shopzen.data.remote.config.PaymobConfig
import shopzen.domain.cart.model.CartItem
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.profile.model.Address
import java.util.UUID

class CheckoutRepositoryImplTest {

    private lateinit var remoteDataSource: CheckoutRemoteDataSource
    private lateinit var paymobRemoteDataSource: PaymobRemoteDataSource
    private lateinit var exchangeRateRemoteDataSource: ExchangeRateRemoteDataSource
    private lateinit var mapper: CheckoutMapper
    private lateinit var paymobMapper: PaymobMapper
    private lateinit var repository: CheckoutRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        remoteDataSource = mockk()
        paymobRemoteDataSource = mockk()
        exchangeRateRemoteDataSource = mockk()
        mapper = mockk()
        paymobMapper = mockk()
        repository = CheckoutRepositoryImpl(
            remoteDataSource = remoteDataSource,
            paymobRemoteDataSource = paymobRemoteDataSource,
            exchangeRateRemoteDataSource = exchangeRateRemoteDataSource,
            mapper = mapper,
            paymobMapper = paymobMapper,
            paymobConfig = PaymobConfig(
                baseUrl = "https://accept.paymob.com",
                publicKey = "public",
                secretKey = "secret",
                currency = "EGP",
                onlineCardIntegrationId = 123,
            ),
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `placeOrder returns success when API responds with order`() = runTest {
        val checkout = mockk<Checkout>(relaxed = true)
        val variables = JsonObject(emptyMap())
        val order = OrderDto(id = "1", name = "#1001")

        coEvery { mapper.toRestOrderCreateBody(checkout) } returns variables
        coEvery { mapper.toOrderConfirmation(order) } returns OrderConfirmation(
            orderId = "1",
            orderNumber = "#1001",
        )
        coEvery { remoteDataSource.createOrder(any(), any()) } returns OrderCreateRestResponse(order = order)

        val result = repository.placeOrder(checkout)

        assertTrue(result.isSuccess)
        val confirmation = result.getOrNull()!!
        assertEquals("1", confirmation.orderId)
        assertEquals("#1001", confirmation.orderNumber)
    }

    @Test
    fun `placeOrder returns failure when API returns user errors`() = runTest {
        val checkout = mockk<Checkout>(relaxed = true)

        coEvery { mapper.toRestOrderCreateBody(checkout) } returns JsonObject(emptyMap())
        coEvery { remoteDataSource.createOrder(any(), any()) } returns OrderCreateRestResponse(order = null)

        val result = repository.placeOrder(checkout)

        assertTrue(result.isFailure)
        assertEquals("Order placement failed with unknown error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `placeOrder passes generated idempotency key to remote data source`() = runTest {
        val checkout = mockk<Checkout>(relaxed = true)
        val keySlot = slot<IdempotencyKey>()

        coEvery { mapper.toRestOrderCreateBody(checkout) } returns JsonObject(emptyMap())
        coEvery { mapper.toOrderConfirmation(any()) } returns OrderConfirmation("1", "#1001")
        coEvery { remoteDataSource.createOrder(any(), capture(keySlot)) } returns OrderCreateRestResponse(
            order = OrderDto(id = "1", name = "#1001"),
        )

        repository.placeOrder(checkout)

        coVerify(exactly = 1) { remoteDataSource.createOrder(any(), any()) }
        UUID.fromString(keySlot.captured.value)
    }

    @Test
    fun `createPaymobPaymentIntention sends unique special reference for each attempt`() = runTest {
        val checkout = sampleCheckout()
        val requests = mutableListOf<PaymobPaymentIntentionRequest>()
        val repository = CheckoutRepositoryImpl(
            remoteDataSource = remoteDataSource,
            paymobRemoteDataSource = paymobRemoteDataSource,
            exchangeRateRemoteDataSource = exchangeRateRemoteDataSource,
            mapper = mapper,
            paymobMapper = PaymobMapper(),
            paymobConfig = PaymobConfig(
                baseUrl = "https://accept.paymob.com",
                publicKey = "public",
                secretKey = "secret",
                currency = "EGP",
                onlineCardIntegrationId = 123,
            ),
        )

        coEvery { exchangeRateRemoteDataSource.getRate("USD", "EGP") } returns 50.0
        coEvery {
            paymobRemoteDataSource.createPaymentIntention(capture(requests))
        } returns PaymobPaymentIntentionResponse(
            clientSecret = "client-secret",
            intentionOrderId = 321L,
            id = "intention-1",
        )

        repository.createPaymobPaymentIntention(checkout)
        repository.createPaymobPaymentIntention(checkout)

        assertEquals(2, requests.size)
        assertTrue(requests.all { it.specialReference?.startsWith("shopzen-") == true })
        assertTrue(requests.none { it.specialReference == "user-1" })
        assertTrue(requests[0].specialReference != requests[1].specialReference)
    }

    private fun sampleCheckout() = Checkout(
        lineItems = listOf(
            CartItem(
                id = "line-1",
                productId = "product-1",
                variantId = "variant-1",
                title = "Blue Hoodie",
                variantTitle = "Size M",
                price = 45.0,
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
        subtotalPrice = 45.0,
        discountAmount = 0.0,
        totalPrice = 45.0,
        currency = "USD",
        appliedCoupon = null,
        selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
        customerId = 123L,
        customerEmail = "ada@example.com",
    )
}
