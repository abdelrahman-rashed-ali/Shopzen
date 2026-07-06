package shopzen.data.profile.repository

import javax.inject.Inject
import shopzen.data.profile.mapper.toDomain
import shopzen.data.profile.remote.RemoteOrderDataSource
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.repository.OrderRepository

class OrderRepositoryImpl @Inject constructor(
    private val remoteOrderDataSource: RemoteOrderDataSource,
) : OrderRepository {
    override suspend fun getOrderHistory(customerId: Long): Result<List<Order>> =
        runCatching {
            remoteOrderDataSource.getOrderHistory(customerId)
                .orders
                .map { it.toDomain() }
        }

    override suspend fun getOrderDetail(orderId: String): Result<Order> =
        runCatching {
            remoteOrderDataSource.getOrderDetail(orderId)
                .order
                .toDomain()
        }
}
