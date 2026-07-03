package shopzen.domain.cart.usecase

import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/** Deletes all cart items for user. Requires ConfirmationDialog before calling. */
class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(userId: String): Result<Unit> = repository.clearCart(userId)
}
