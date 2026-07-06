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

class GetOrderDetailUseCaseTest {

    private val repository: OrderRepository = mockk()
    private val useCase = GetOrderDetailUseCase(repository)

    @Test
    fun `invoke delegates to repository and returns order`() = runTest {
        val order = sampleOrder()
        coEvery { repository.getOrderDetail("1001") } returns Result.success(order)

        val result = useCase("1001")

        assertEquals(order, result.getOrNull())
        coVerify(exactly = 1) { repository.getOrderDetail("1001") }
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        val failure = IllegalStateException("network failed")
        coEvery { repository.getOrderDetail("1001") } returns Result.failure(failure)

        val result = useCase("1001")

        assertTrue(result.isFailure)
        assertSame(failure, result.exceptionOrNull())
    }

    private fun sampleOrder() = Order(
        id = "1001",
        orderNumber = "#1001",
        totalPrice = 125.50,
        currency = "USD",
        financialStatus = "paid",
        fulfillmentStatus = "fulfilled",
        createdAt = "2026-07-01T10:00:00Z",
        lineItems = emptyList(),
    )
}
