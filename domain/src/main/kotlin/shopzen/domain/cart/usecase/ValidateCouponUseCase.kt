package shopzen.domain.cart.usecase

import shopzen.domain.cart.model.CouponValidationResult
import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/** Validates a coupon code through the cart repository contract. */
class ValidateCouponUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    /** Returns coupon validation data or a repository failure. */
    suspend operator fun invoke(code: String): Result<CouponValidationResult> =
        repository.validateCoupon(code.trim())
}
