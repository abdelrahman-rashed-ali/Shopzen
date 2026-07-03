package shopzen.domain.cart.usecase

import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/** Removes a single cart item. Requires ConfirmationDialog in UI before calling. */
class RemoveFromCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(itemId: String, userId: String): Result<Unit> =
        repository.removeFromCart(itemId, userId)
}
