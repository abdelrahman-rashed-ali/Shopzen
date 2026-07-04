package shopzen.domain.checkout.usecase

import shopzen.domain.checkout.model.PaymentMethod
import javax.inject.Inject

/** Returns payment methods allowed for the current checkout total. */
class GetAvailablePaymentMethodsUseCase @Inject constructor(
    private val validateCashLimitUseCase: ValidateCashLimitUseCase,
) {
    /** Includes cash on delivery only while the total is within the COD limit. */
    operator fun invoke(totalPrice: Double): List<PaymentMethod> =
        if (validateCashLimitUseCase(totalPrice)) {
            listOf(PaymentMethod.CASH_ON_DELIVERY, PaymentMethod.ONLINE_PAYMENT)
        } else {
            listOf(PaymentMethod.ONLINE_PAYMENT)
        }
}
