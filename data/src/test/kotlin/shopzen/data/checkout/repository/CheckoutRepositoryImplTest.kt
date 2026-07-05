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
import shopzen.data.checkout.remote.CheckoutRemoteDataSource
import shopzen.data.checkout.remote.IdempotencyKey
import shopzen.data.checkout.remote.dto.OrderDto
import shopzen.data.checkout.remote.dto.OrderCreateRestResponse
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import java.util.UUID

class CheckoutRepositoryImplTest {

    private lateinit var remoteDataSource: CheckoutRemoteDataSource
    private lateinit var mapper: CheckoutMapper
    private lateinit var repository: CheckoutRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        remoteDataSource = mockk()
        mapper = mockk()
        repository = CheckoutRepositoryImpl(remoteDataSource, mapper)
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
}
