package shopzen.domain.cart.model

sealed class CouponValidationResult {
    data class Valid(
        val code: String,
        val discountPercent: Double?,
        val discountFixed: Double?,
    ) : CouponValidationResult()

    data class Invalid(val reason: String) : CouponValidationResult()
}
