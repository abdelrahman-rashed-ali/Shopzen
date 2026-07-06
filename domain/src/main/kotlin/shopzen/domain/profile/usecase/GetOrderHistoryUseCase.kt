package shopzen.domain.profile.usecase

import javax.inject.Inject
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.repository.OrderRepository

/**
 * Loads all past Shopify orders for a customer.
 */
class GetOrderHistoryUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    /**
     * Returns customer orders, preserving repository success or failure.
     */
    suspend operator fun invoke(customerId: Long): Result<List<Order>> =
        repository.getOrderHistory(customerId)
}
