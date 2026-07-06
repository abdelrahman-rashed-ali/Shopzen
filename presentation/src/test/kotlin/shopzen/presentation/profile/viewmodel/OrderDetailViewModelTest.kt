package shopzen.presentation.profile.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.repository.OrderRepository
import shopzen.domain.profile.usecase.GetOrderDetailUseCase
import shopzen.presentation.MainDispatcherRule
import shopzen.presentation.R
import shopzen.presentation.common.util.UiText
import shopzen.presentation.profile.intent.OrderDetailIntent

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val orderRepository: OrderRepository = mockk()
    private val getOrderDetailUseCase = GetOrderDetailUseCase(orderRepository)

    @Test
    fun `load order populates order on success`() = runTest {
        val order = sampleOrder()
        coEvery { orderRepository.getOrderDetail("1001") } returns Result.success(order)

        val viewModel = createViewModel()
        viewModel.processIntent(OrderDetailIntent.LoadOrder("1001"))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(order, viewModel.state.value.order)
        assertTrue(viewModel.state.value.error == null)
        coVerify(exactly = 1) { orderRepository.getOrderDetail("1001") }
    }

    @Test
    fun `load order maps repository failure to localized load error`() = runTest {
        coEvery { orderRepository.getOrderDetail("1001") } returns Result.failure(
            IllegalStateException("server failed")
        )

        val viewModel = createViewModel()
        viewModel.processIntent(OrderDetailIntent.LoadOrder("1001"))
        advanceUntilIdle()

        val error = viewModel.state.value.error as UiText.StringResource
        assertEquals(R.string.order_detail_error_load_failed, error.resId)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `retry reloads last order id`() = runTest {
        coEvery { orderRepository.getOrderDetail("1001") } returns Result.success(sampleOrder())

        val viewModel = createViewModel()
        viewModel.processIntent(OrderDetailIntent.LoadOrder("1001"))
        advanceUntilIdle()
        viewModel.processIntent(OrderDetailIntent.Retry)
        advanceUntilIdle()

        coVerify(exactly = 2) { orderRepository.getOrderDetail("1001") }
    }

    private fun createViewModel() = OrderDetailViewModel(
        getOrderDetailUseCase = getOrderDetailUseCase,
    )

    private fun sampleOrder() = Order(
        id = "1001",
        orderNumber = "#1001",
        totalPrice = 45.0,
        currency = "USD",
        financialStatus = "paid",
        fulfillmentStatus = "fulfilled",
        createdAt = "2026-07-01T10:00:00Z",
        lineItems = emptyList(),
    )
}
