package shopzen.presentation.profile.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import shopzen.domain.customer.usecase.GetCurrentShopifyCustomerIdUseCase
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.repository.OrderRepository
import shopzen.domain.profile.usecase.GetOrderHistoryUseCase
import shopzen.presentation.MainDispatcherRule
import shopzen.presentation.R
import shopzen.presentation.common.util.UiText
import shopzen.presentation.profile.intent.OrderHistoryIntent

class OrderHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCurrentShopifyCustomerIdUseCase: GetCurrentShopifyCustomerIdUseCase = mockk()
    private val orderRepository: OrderRepository = mockk()
    private val getOrderHistoryUseCase = GetOrderHistoryUseCase(orderRepository)

    @Test
    fun `load orders populates orders on success`() = runTest {
        val orders = listOf(sampleOrder())
        coEvery { getCurrentShopifyCustomerIdUseCase() } returns Result.success(42L)
        coEvery { orderRepository.getOrderHistory(42L) } returns Result.success(orders)

        val viewModel = createViewModel()
        viewModel.processIntent(OrderHistoryIntent.LoadOrders)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(orders, viewModel.state.value.orders)
        assertTrue(viewModel.state.value.error == null)
        coVerify(exactly = 1) { orderRepository.getOrderHistory(42L) }
    }

    @Test
    fun `load orders maps auth failure to localized error`() = runTest {
        coEvery { getCurrentShopifyCustomerIdUseCase() } returns Result.failure(
            IllegalStateException("No authenticated user")
        )

        val viewModel = createViewModel()
        viewModel.processIntent(OrderHistoryIntent.LoadOrders)
        advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.StringResource
        assertEquals(R.string.order_history_error_auth_required, error.resId)
        assertTrue(viewModel.state.value.orders.isEmpty())
    }

    @Test
    fun `load orders maps repository failure to localized load error`() = runTest {
        coEvery { getCurrentShopifyCustomerIdUseCase() } returns Result.success(42L)
        coEvery { orderRepository.getOrderHistory(42L) } returns Result.failure(
            IllegalStateException("server failed")
        )

        val viewModel = createViewModel()
        viewModel.processIntent(OrderHistoryIntent.LoadOrders)
        advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.StringResource
        assertEquals(R.string.order_history_error_load_failed, error.resId)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `retry reloads order history`() = runTest {
        coEvery { getCurrentShopifyCustomerIdUseCase() } returns Result.success(42L)
        coEvery { orderRepository.getOrderHistory(42L) } returns Result.success(listOf(sampleOrder()))

        val viewModel = createViewModel()
        viewModel.processIntent(OrderHistoryIntent.Retry)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.orders.size)
        coVerify(exactly = 1) { orderRepository.getOrderHistory(42L) }
    }

    private fun createViewModel() = OrderHistoryViewModel(
        getCurrentShopifyCustomerIdUseCase = getCurrentShopifyCustomerIdUseCase,
        getOrderHistoryUseCase = getOrderHistoryUseCase,
    )

    private fun sampleOrder() = Order(
        id = "order-1",
        orderNumber = "#1001",
        totalPrice = 45.0,
        currency = "USD",
        financialStatus = "paid",
        fulfillmentStatus = "fulfilled",
        createdAt = "2026-07-01T10:00:00Z",
        lineItems = emptyList(),
    )
}
