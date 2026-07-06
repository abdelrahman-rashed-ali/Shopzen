package shopzen.domain.profile.usecase

import javax.inject.Inject
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.repository.OrderRepository

/**
 * Loads one Shopify order with its detail fields and line items.
 */
class GetOrderDetailUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    /**
     * Returns an order detail result, preserving repository success or failure.
     */
    suspend operator fun invoke(orderId: String): Result<Order> =
        repository.getOrderDetail(orderId)
}
