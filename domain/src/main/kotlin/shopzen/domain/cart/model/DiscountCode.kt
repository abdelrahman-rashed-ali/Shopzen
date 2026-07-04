package shopzen.domain.cart.model

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}

data class DiscountCode(
    val code: String,
    val discountType: DiscountType,
    val value: Double,
)
