package shopzen.domain.profile.repository

import shopzen.domain.profile.model.Order

/**
 * Repository contract for Shopify customer orders.
 */
interface OrderRepository {
    /**
     * Fetches past Shopify orders for [customerId].
     */
    suspend fun getOrderHistory(customerId: Long): Result<List<Order>>

    /**
     * Fetches the full Shopify order identified by [orderId].
     */
    suspend fun getOrderDetail(orderId: String): Result<Order>
}
