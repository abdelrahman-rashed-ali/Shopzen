package shopzen.domain.cart.usecase

import shopzen.domain.cart.model.CouponValidationResult
import javax.inject.Inject

/**
 * Validates a coupon code against Shopify price rules.
 * Mock returns a valid 10% discount until data layer is wired.
 */
class ValidateCouponUseCase @Inject constructor() {
    suspend operator fun invoke(code: String): Result<CouponValidationResult> {
        if (code.isBlank()) return Result.failure(IllegalArgumentException("Code is empty"))
        // Mock: "SAVE10" is valid, everything else is invalid
        return if (code.uppercase() == "SAVE10") {
            Result.success(
                CouponValidationResult.Valid(
                    code = code.uppercase(),
                    discountPercent = 10.0,
                    discountFixed = null,
                )
            )
        } else {
            Result.success(CouponValidationResult.Invalid("Invalid or expired coupon code"))
        }
    }
}
