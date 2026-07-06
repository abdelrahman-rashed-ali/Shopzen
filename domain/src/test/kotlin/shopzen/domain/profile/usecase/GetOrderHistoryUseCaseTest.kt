package shopzen.domain.profile.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.repository.OrderRepository

class GetOrderHistoryUseCaseTest {

    private val repository: OrderRepository = mockk()
    private val useCase = GetOrderHistoryUseCase(repository)

    @Test
    fun `invoke delegates to repository and returns orders`() = runTest {
        val orders = listOf(
            Order(
                id = "1001",
                orderNumber = "#1001",
                totalPrice = 125.50,
                currency = "USD",
                financialStatus = "paid",
                fulfillmentStatus = "fulfilled",
                createdAt = "2026-07-01T10:00:00Z",
                lineItems = emptyList(),
            )
        )
        coEvery { repository.getOrderHistory(42L) } returns Result.success(orders)

        val result = useCase(42L)

        assertEquals(orders, result.getOrNull())
        coVerify(exactly = 1) { repository.getOrderHistory(42L) }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val failure = IllegalStateException("network failed")
        coEvery { repository.getOrderHistory(42L) } returns Result.failure(failure)

        val result = useCase(42L)

        assertTrue(result.isFailure)
        assertSame(failure, result.exceptionOrNull())
    }
}
