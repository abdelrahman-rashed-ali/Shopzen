package shopzen.domain.cart.usecase

import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/**
 * Remote use case to remove an applied coupon via Shopify Storefront API.
 */
class RemoveCouponUseCase @Inject constructor(
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(userId: String, code: String): Result<Unit> {
        return cartRepository.removeCoupon(userId, code)
    }
}
