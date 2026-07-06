package shopzen.data.checkout.repository

import android.util.Log
import shopzen.data.checkout.mapper.CheckoutMapper
import shopzen.data.checkout.mapper.PaymobMapper
import shopzen.data.checkout.remote.CheckoutRemoteDataSource
import shopzen.data.checkout.remote.ExchangeRateRemoteDataSource
import shopzen.data.checkout.remote.IdempotencyKey
import shopzen.data.checkout.remote.PaymobRemoteDataSource
import shopzen.data.remote.config.PaymobConfig
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import shopzen.domain.checkout.model.PaymobPaymentIntention
import shopzen.domain.checkout.repository.CheckoutRepository
import kotlinx.coroutines.CancellationException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckoutRepositoryImpl @Inject constructor(
    private val remoteDataSource: CheckoutRemoteDataSource,
    private val paymobRemoteDataSource: PaymobRemoteDataSource,
    private val exchangeRateRemoteDataSource: ExchangeRateRemoteDataSource,
    private val mapper: CheckoutMapper,
    private val paymobMapper: PaymobMapper,
    private val paymobConfig: PaymobConfig,
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

    override suspend fun createPaymobPaymentIntention(
        checkout: Checkout,
    ): Result<PaymobPaymentIntention> {
        return try {
            if (paymobConfig.secretKey.isBlank() || paymobConfig.publicKey.isBlank()) {
                return Result.failure(IllegalStateException("Paymob keys are missing"))
            }
            if (paymobConfig.enabledPaymentMethodIds.isEmpty()) {
                return Result.failure(
                    IllegalStateException(
                        "Paymob payment method integration IDs are missing. Add " +
                            "PAYMOB_ONLINE_CARD_INTEGRATION_ID to local.properties.",
                    )
                )
            }
            val exchangeRate = exchangeRateRemoteDataSource.getRate(
                baseCurrency = checkout.currency,
                targetCurrency = paymobConfig.normalizedCurrency,
            )
            val request = paymobMapper.toPaymentIntentionRequest(
                checkout = checkout,
                config = paymobConfig,
                specialReference = createPaymobSpecialReference(),
                exchangeRate = exchangeRate,
            )
            Log.d(
                TAG,
                "createPaymobPaymentIntention: amount=${request.amount}, " +
                    "currency=${request.currency}, sourceCurrency=${checkout.currency}, " +
                    "exchangeRate=$exchangeRate, methods=${request.paymentMethods}",
            )
            val response = paymobRemoteDataSource.createPaymentIntention(request)
            Result.success(paymobMapper.toPaymentIntention(response, paymobConfig))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "createPaymobPaymentIntention: failed", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "CheckoutRepository"

        fun createPaymobSpecialReference(): String = "shopzen-${UUID.randomUUID()}"
    }
}
