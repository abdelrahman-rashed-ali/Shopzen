package shopzen.data.checkout.repository

import shopzen.data.checkout.mapper.CheckoutMapper
import shopzen.data.checkout.remote.CheckoutRemoteDataSource
import shopzen.data.checkout.remote.IdempotencyKey
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import shopzen.domain.checkout.repository.CheckoutRepository
import kotlinx.coroutines.CancellationException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckoutRepositoryImpl @Inject constructor(
    private val remoteDataSource: CheckoutRemoteDataSource,
    private val mapper: CheckoutMapper
) : CheckoutRepository {

    override suspend fun placeOrder(checkout: Checkout): Result<OrderConfirmation> {
        return try {
            val idempotencyKey = IdempotencyKey(UUID.randomUUID().toString())
            val response = remoteDataSource.createOrder(
                variables = mapper.toOrderCreateVariables(checkout),
                idempotencyKey = idempotencyKey,
            )

            val payload = response.data?.orderCreate
            val errors = response.errors

            if (payload?.userErrors?.isNotEmpty() == true) {
                val errorMessage = payload.userErrors.first().message
                Result.failure(Exception(errorMessage))
            } else if (payload?.order != null) {
                Result.success(mapper.toOrderConfirmation(payload.order))
            } else if (errors?.isNotEmpty() == true) {
                Result.failure(Exception(errors.first().message))
            } else {
                Result.failure(Exception("Order placement failed with unknown error"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
