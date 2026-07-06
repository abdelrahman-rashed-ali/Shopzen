package shopzen.domain.cart.usecase

import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/**
 * Remote use case to apply a discount code via Shopify Storefront API.
 */
class ApplyCouponUseCase @Inject constructor(
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(userId: String, code: String): Result<Unit> =
        cartRepository.applyCoupon(userId, code)
}
