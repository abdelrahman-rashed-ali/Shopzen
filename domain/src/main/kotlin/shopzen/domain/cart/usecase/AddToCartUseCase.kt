package shopzen.domain.cart.usecase

import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/** Inserts or increments a cart item. Validates stock before insert via the repository. */
class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    suspend operator fun invoke(item: CartItem): Result<Unit> = repository.addToCart(item)
}
