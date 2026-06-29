package shopzen.domain.cart.usecase

import javax.inject.Inject

/** Clamps quantity to [1, maxQuantity] and saves. Mock until data layer. */
class UpdateCartItemQuantityUseCase @Inject constructor() {
    suspend operator fun invoke(itemId: String, quantity: Int, userId: String): Result<Unit> =
        Result.success(Unit)
}
