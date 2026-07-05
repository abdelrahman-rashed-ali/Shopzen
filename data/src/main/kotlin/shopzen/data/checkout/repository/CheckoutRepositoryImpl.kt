package shopzen.data.checkout.repository

import android.util.Log
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
            val body = mapper.toRestOrderCreateBody(checkout)
            Log.d(
                TAG,
                "placeOrder: start items=${checkout.lineItems.size}, total=${checkout.totalPrice}, " +
                    "currency=${checkout.currency}, payment=${checkout.selectedPaymentMethod}, " +
                    "coupon=${checkout.appliedCoupon?.code}, idempotencyKey=${idempotencyKey.value}",
            )
            Log.d(TAG, "placeOrder: REST body=$body")
            val response = remoteDataSource.createOrder(
                body = body,
                idempotencyKey = idempotencyKey,
            )

            if (response.order != null) {
                Log.d(
                    TAG,
                    "placeOrder: success orderId=${response.order.id}, orderNumber=${response.order.name}",
                )
                Result.success(mapper.toOrderConfirmation(response.order))
            } else {
                Log.e(TAG, "placeOrder: empty REST response order=null")
                Result.failure(Exception("Order placement failed with unknown error"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "placeOrder: failed", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "CheckoutRepository"
    }
}
