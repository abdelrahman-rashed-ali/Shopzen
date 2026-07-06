package shopzen.domain.checkout.usecase

import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.PaymobPaymentIntention
import shopzen.domain.checkout.repository.CheckoutRepository
import javax.inject.Inject

/** Creates a Paymob mobile SDK payment intention for an online checkout. */
class CreatePaymobPaymentIntentionUseCase @Inject constructor(
    private val repository: CheckoutRepository,
) {
    /** Delegates Paymob intention creation and returns the client launch data. */
    suspend operator fun invoke(checkout: Checkout): Result<PaymobPaymentIntention> =
        repository.createPaymobPaymentIntention(checkout)
}
