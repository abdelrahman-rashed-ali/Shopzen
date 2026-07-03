package shopzen.domain.cart.usecase

import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/** Clamps quantity to [1, maxQuantity] and persists via the repository. */
class UpdateCartItemQuantityUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(itemId: String, quantity: Int, userId: String): Result<Unit> =
        repository.updateItemQuantity(itemId, quantity, userId)
}
