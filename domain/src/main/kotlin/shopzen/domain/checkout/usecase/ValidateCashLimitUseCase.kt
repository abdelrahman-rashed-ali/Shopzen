package shopzen.domain.checkout.usecase

import shopzen.domain.common.Constants
import javax.inject.Inject

/** Checks whether a checkout total is eligible for cash on delivery. */
class ValidateCashLimitUseCase @Inject constructor() {
    /** Returns true when [totalPrice] does not exceed the COD limit. */
    operator fun invoke(totalPrice: Double): Boolean =
        totalPrice <= Constants.MAX_COD_AMOUNT
}
