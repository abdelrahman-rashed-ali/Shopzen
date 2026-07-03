package shopzen.domain.cart.usecase

import shopzen.domain.cart.model.CouponValidationResult
import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/**
 * Validates a coupon code against Shopify price rules via the repository.
 */
class ValidateCouponUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(code: String): Result<CouponValidationResult> {
        if (code.isBlank()) return Result.failure(IllegalArgumentException("Code is empty"))
        return repository.validateCoupon(code)
    }
}
