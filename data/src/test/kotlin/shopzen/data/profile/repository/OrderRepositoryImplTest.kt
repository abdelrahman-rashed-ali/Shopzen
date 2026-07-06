package shopzen.data.profile.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import shopzen.data.profile.remote.RemoteOrderDataSource
import shopzen.data.profile.remote.dto.OrderDetailResponseDto
import shopzen.data.profile.remote.dto.OrderDto
import shopzen.data.profile.remote.dto.OrderHistoryResponseDto

class OrderRepositoryImplTest {

    private val remoteOrderDataSource: RemoteOrderDataSource = mockk()
    private val repository = OrderRepositoryImpl(remoteOrderDataSource)

    @Test
    fun `getOrderDetail maps remote order to domain`() = runTest {
        coEvery { remoteOrderDataSource.getOrderDetail("10") } returns OrderDetailResponseDto(
            order = OrderDto(
                id = 10L,
                name = "#1010",
                totalPrice = "99.95",
                currency = "USD",
                financialStatus = "paid",
                fulfillmentStatus = "fulfilled",
                createdAt = "2026-07-01T10:15:30Z",
                lineItems = emptyList(),
            )
        )

        val result = repository.getOrderDetail("10")

        assertTrue(result.isSuccess)
        assertEquals("#1010", result.getOrThrow().orderNumber)
        assertEquals(99.95, result.getOrThrow().totalPrice, 0.0)
        coVerify(exactly = 1) { remoteOrderDataSource.getOrderDetail("10") }
    }

    @Test
    fun `getOrderDetail wraps remote failure`() = runTest {
        val failure = IllegalStateException("server failed")
        coEvery { remoteOrderDataSource.getOrderDetail("10") } throws failure

        val result = repository.getOrderDetail("10")

        assertTrue(result.isFailure)
        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun `getOrderHistory still maps remote orders`() = runTest {
        coEvery { remoteOrderDataSource.getOrderHistory(42L) } returns OrderHistoryResponseDto(
            orders = listOf(OrderDto(id = 10L, name = "#1010")),
        )

        val result = repository.getOrderHistory(42L)

        assertEquals("#1010", result.getOrThrow().first().orderNumber)
        coVerify(exactly = 1) { remoteOrderDataSource.getOrderHistory(42L) }
    }
}
